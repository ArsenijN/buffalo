//! Control channel: WebSocket, JSON, separate port from the video channel (SPECS.md 1.1).
//! The OBS plugin is the client here too (same as the video channel) -- the phone runs both
//! listeners... actually per the spec, on connect the *phone* sends "hello" once the OBS
//! plugin opens the socket, so we're a WS client dialing out to the phone, same direction as
//! the video channel. Keeps both connections symmetric and firewall-simple.

use futures_util::{SinkExt, StreamExt};
use serde::{Deserialize, Serialize};
use tokio::runtime::Runtime;
use tokio_tungstenite::connect_async;
use tokio_tungstenite::tungstenite::Message;

#[derive(Debug, Clone)]
pub enum ControlEvent {
    Hello { device: String, resolution: String },
    Connected,
    Disconnected,
}

#[derive(Serialize)]
#[serde(tag = "type", rename_all = "snake_case")]
enum OutgoingMessage {
    SetBitrate { value: u32 },
    Ping,
}

#[derive(Deserialize)]
#[serde(tag = "type", rename_all = "snake_case")]
enum IncomingMessage {
    Hello { device: String, resolution: String },
    Pong,
    #[serde(other)]
    Unknown,
}

pub struct ControlClient {
    runtime: Runtime,
    cmd_tx: Option<tokio::sync::mpsc::UnboundedSender<OutgoingMessage>>,
}

impl ControlClient {
    /// Connects to ws://host:port and starts pumping messages. `on_event` is called from the
    /// background tokio runtime thread -- if you touch OBS state in it, make sure that's safe
    /// to do off the main OBS thread (usually means marshaling through a channel back to your
    /// video-tick callback rather than calling obs_source_* directly here).
    pub fn start(host: String, port: u16, on_event: impl Fn(ControlEvent) + Send + 'static) -> Self {
        let runtime = tokio::runtime::Builder::new_multi_thread()
            .worker_threads(1)
            .enable_all()
            .build()
            .expect("failed to build control channel runtime");

        let (cmd_tx, mut cmd_rx) = tokio::sync::mpsc::unbounded_channel::<OutgoingMessage>();

        runtime.spawn(async move {
            loop {
                let url = format!("ws://{host}:{port}");
                match connect_async(&url).await {
                    Ok((ws_stream, _)) => {
                        on_event(ControlEvent::Connected);
                        let (mut write, mut read) = ws_stream.split();

                        loop {
                            tokio::select! {
                                msg = read.next() => {
                                    match msg {
                                        Some(Ok(Message::Text(text))) => {
                                            handle_incoming(&text, &on_event);
                                        }
                                        Some(Ok(Message::Ping(_))) | Some(Ok(Message::Pong(_))) => {}
                                        Some(Ok(_)) => {}
                                        Some(Err(e)) => {
                                            eprintln!("[buffalo] control channel read error: {e}");
                                            break;
                                        }
                                        None => break,
                                    }
                                }
                                cmd = cmd_rx.recv() => {
                                    match cmd {
                                        Some(outgoing) => {
                                            let text = serde_json::to_string(&outgoing).unwrap_or_default();
                                            if write.send(Message::Text(text)).await.is_err() {
                                                break;
                                            }
                                        }
                                        None => return, // ControlClient dropped
                                    }
                                }
                            }
                        }

                        on_event(ControlEvent::Disconnected);
                    }
                    Err(e) => {
                        eprintln!("[buffalo] control connect to {url} failed: {e}");
                    }
                }

                tokio::time::sleep(std::time::Duration::from_secs(1)).await;
            }
        });

        Self { runtime, cmd_tx: Some(cmd_tx) }
    }

    /// Send a bitrate change to the phone -- wires up the "change bitrate" button from
    /// SPECS.md 1.3's OBS plugin checklist.
    pub fn set_bitrate(&self, bps: u32) {
        if let Some(tx) = &self.cmd_tx {
            let _ = tx.send(OutgoingMessage::SetBitrate { value: bps });
        }
    }
}

fn handle_incoming(text: &str, on_event: &impl Fn(ControlEvent)) {
    match serde_json::from_str::<IncomingMessage>(text) {
        Ok(IncomingMessage::Hello { device, resolution }) => {
            on_event(ControlEvent::Hello { device, resolution });
        }
        Ok(IncomingMessage::Pong) => {}
        Ok(IncomingMessage::Unknown) => {}
        Err(e) => eprintln!("[buffalo] bad control message '{text}': {e}"),
    }
}

// Also referenced so `runtime` isn't flagged unused when this module is compiled standalone
// during early development.
#[allow(dead_code)]
fn _keep_runtime_alive(c: &ControlClient) -> &Runtime {
    &c.runtime
}
