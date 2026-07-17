//! Control channel SERVER: WebSocket, JSON (SPECS.md 1.1), phone dials in. Flipped from an
//! earlier client-dials-out design -- see the architecture note in lib.rs.

use futures_util::{SinkExt, StreamExt};
use serde::{Deserialize, Serialize};
use tokio::net::TcpListener;
use tokio::runtime::Runtime;
use tokio::sync::Mutex as AsyncMutex;
use tokio_tungstenite::tungstenite::Message;
use std::sync::Arc;

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

type WsWrite = futures_util::stream::SplitSink<
    tokio_tungstenite::WebSocketStream<tokio::net::TcpStream>,
    Message,
>;

pub struct ControlServer {
    runtime: Option<Runtime>,
    // Whichever phone is currently connected, if any -- set_bitrate() sends to this. Only one
    // phone streams at a time in v0, so "the current connection" is an unambiguous target.
    // tokio::sync::Mutex (not std) specifically because we hold this across a `.await` when
    // sending.
    current_writer: Arc<AsyncMutex<Option<WsWrite>>>,
}

impl ControlServer {
    /// Binds `0.0.0.0:port` and accepts the phone's control-channel connection. Like the video
    /// server, this loops back to accepting after a disconnect rather than dying.
    pub fn start(port: u16, on_event: impl Fn(ControlEvent) + Send + Sync + 'static) -> Self {
        let runtime = tokio::runtime::Builder::new_multi_thread()
            .worker_threads(1)
            .enable_all()
            .build()
            .expect("failed to build control channel runtime");

        let current_writer: Arc<AsyncMutex<Option<WsWrite>>> = Arc::new(AsyncMutex::new(None));
        let current_writer_task = current_writer.clone();
        let on_event = Arc::new(on_event);

        runtime.spawn(async move {
            let listener = match TcpListener::bind(("0.0.0.0", port)).await {
                Ok(l) => l,
                Err(e) => {
                    eprintln!("[buffalo] failed to bind control port {port}: {e}");
                    return;
                }
            };
            println!("[buffalo] control server listening on :{port}, waiting for the phone...");

            loop {
                let (stream, addr) = match listener.accept().await {
                    Ok(pair) => pair,
                    Err(e) => {
                        eprintln!("[buffalo] control accept error: {e}");
                        tokio::time::sleep(std::time::Duration::from_millis(500)).await;
                        continue;
                    }
                };

                let ws_stream = match tokio_tungstenite::accept_async(stream).await {
                    Ok(ws) => ws,
                    Err(e) => {
                        eprintln!("[buffalo] control WebSocket handshake with {addr} failed: {e}");
                        continue;
                    }
                };

                println!("[buffalo] control channel connected: {addr}");
                on_event(ControlEvent::Connected);

                let (write, mut read) = ws_stream.split();
                *current_writer_task.lock().await = Some(write);

                while let Some(msg) = read.next().await {
                    match msg {
                        Ok(Message::Text(text)) => handle_incoming(&text, on_event.as_ref()),
                        Ok(Message::Ping(_)) | Ok(Message::Pong(_)) => {}
                        Ok(_) => {}
                        Err(e) => {
                            eprintln!("[buffalo] control channel read error: {e}");
                            break;
                        }
                    }
                }

                *current_writer_task.lock().await = None;
                on_event(ControlEvent::Disconnected);
                println!("[buffalo] control server listening on :{port}, waiting for the phone...");
            }
        });

        Self { runtime: Some(runtime), current_writer }
    }

    /// Send a bitrate change to whichever phone is currently connected -- wires up the "change
    /// bitrate" button from SPECS.md 1.3's OBS plugin checklist. No-op if nothing's connected
    /// (or if we're already mid-shutdown, in the narrow window Drop below is running).
    pub fn set_bitrate(&self, bps: u32) {
        let Some(runtime) = self.runtime.as_ref() else { return };
        let current_writer = self.current_writer.clone();
        runtime.spawn(async move {
            let mut guard = current_writer.lock().await;
            if let Some(write) = guard.as_mut() {
                let text = serde_json::to_string(&OutgoingMessage::SetBitrate { value: bps })
                    .unwrap_or_default();
                let _ = write.send(Message::Text(text)).await;
            }
        });
    }
}

impl Drop for ControlServer {
    fn drop(&mut self) {
        // shutdown_background() takes ownership and returns immediately, abandoning any
        // in-flight tasks (our accept loop never returns on its own -- it's an infinite loop by
        // design) without waiting for them. The default Runtime::drop() instead blocks the
        // calling thread until shutdown completes, which is a real risk here: OBS's source
        // destroy callback runs on its own thread, and there's no guarantee blocking there is
        // safe or bounded. This is the leading fix for sources not fully deleting.
        if let Some(runtime) = self.runtime.take() {
            runtime.shutdown_background();
        }
    }
}

fn handle_incoming(text: &str, on_event: &(impl Fn(ControlEvent) + Send + Sync + 'static)) {
    match serde_json::from_str::<IncomingMessage>(text) {
        Ok(IncomingMessage::Hello { device, resolution }) => {
            on_event(ControlEvent::Hello { device, resolution });
        }
        Ok(IncomingMessage::Pong) => {}
        Ok(IncomingMessage::Unknown) => {}
        Err(e) => eprintln!("[buffalo] bad control message '{text}': {e}"),
    }
}
