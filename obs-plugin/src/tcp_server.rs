//! Video channel SERVER: the phone dials in here (SPECS.md 1.1's length-prefixed Annex-B
//! frame protocol, unchanged). Flipped from an earlier client-dials-out design -- see the
//! architecture note in lib.rs for why. Runs on its own OS thread (plain blocking I/O; no
//! benefit from an async executor for a single hot byte-shuffling loop).

use std::io::{ErrorKind, Read};
use std::net::{TcpListener, TcpStream};
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::Arc;
use std::thread;
use std::time::Duration;

use openh264::decoder::Decoder;
use openh264::formats::YUVSource;
use openh264::nal_units;

pub struct DecodedFrame {
    pub width: u32,
    pub height: u32,
    /// Planar I420 (Y, then U, then V), ready for obs_source_output_video2 with
    /// VIDEO_FORMAT_I420.
    pub i420: Vec<u8>,
}

pub struct TcpVideoServer {
    running: Arc<AtomicBool>,
}

impl TcpVideoServer {
    /// Binds `0.0.0.0:port` and waits for the phone to connect. Reconnect-tolerant: if the
    /// phone's connection drops (app closed, streaming stopped, Wi-Fi hiccup), this goes back
    /// to listening for the next connection rather than dying -- no plugin restart needed
    /// between takes. `on_connection_change` fires true right after a phone connects and false
    /// when it disconnects, for status reporting.
    pub fn start(
        port: u16,
        on_frame: impl Fn(DecodedFrame) + Send + 'static,
        on_connection_change: impl Fn(bool) + Send + 'static,
    ) -> Self {
        let running = Arc::new(AtomicBool::new(true));
        let running_thread = running.clone();

        thread::spawn(move || run(port, &running_thread, on_frame, on_connection_change));

        Self { running }
    }

    pub fn stop(&mut self) {
        self.running.store(false, Ordering::SeqCst);
    }
}

impl Drop for TcpVideoServer {
    fn drop(&mut self) {
        self.stop();
    }
}

fn run(
    port: u16,
    running: &AtomicBool,
    on_frame: impl Fn(DecodedFrame),
    on_connection_change: impl Fn(bool),
) {
    let listener = match TcpListener::bind(("0.0.0.0", port)) {
        Ok(l) => l,
        Err(e) => {
            eprintln!("[buffalo] failed to bind video port {port}: {e}");
            return;
        }
    };
    // Nonblocking accept() so this thread can still notice `running` flipping to false while
    // waiting for a phone to connect, instead of being stuck in a blocking accept() forever.
    if let Err(e) = listener.set_nonblocking(true) {
        eprintln!("[buffalo] failed to set video listener nonblocking: {e}");
        return;
    }

    println!("[buffalo] video server listening on :{port}, waiting for the phone...");

    while running.load(Ordering::SeqCst) {
        match listener.accept() {
            Ok((mut stream, addr)) => {
                println!("[buffalo] phone connected: {addr}");
                on_connection_change(true);
                let _ = stream.set_nonblocking(false); // back to blocking for the per-frame read loop
                let _ = stream.set_read_timeout(Some(Duration::from_millis(500)));
                let _ = stream.set_nodelay(true);

                let mut decoder = match Decoder::new() {
                    Ok(d) => d,
                    Err(e) => {
                        eprintln!("[buffalo] failed to init openh264 decoder: {e}");
                        return;
                    }
                };

                while running.load(Ordering::SeqCst) {
                    match read_frame(&mut stream) {
                        Ok(Some(access_unit)) => {
                            for nal in nal_units(&access_unit) {
                                match decoder.decode(nal) {
                                    Ok(Some(yuv)) => on_frame(to_decoded_frame(&yuv)),
                                    Ok(None) => {}
                                    Err(e) => eprintln!("[buffalo] decode error: {e}"),
                                }
                            }
                        }
                        Ok(None) => continue, // read timeout, loop to re-check `running`
                        Err(e) => {
                            eprintln!("[buffalo] phone disconnected: {e}");
                            break; // back to accept() for the next connection
                        }
                    }
                }

                on_connection_change(false);
                println!("[buffalo] video server listening on :{port}, waiting for the phone...");
            }
            Err(e) if e.kind() == ErrorKind::WouldBlock => {
                thread::sleep(Duration::from_millis(200));
            }
            Err(e) => {
                eprintln!("[buffalo] accept error: {e}");
                thread::sleep(Duration::from_millis(500));
            }
        }
    }
}

/// Reads one length-prefixed frame. Returns Ok(None) if nothing has arrived yet in this
/// read-timeout window -- distinct from a real error (connection dead). Accumulates partial
/// reads across timeouts rather than discarding progress, since a 500ms read timeout can
/// legitimately fire mid-frame on a slow Wi-Fi link.
fn read_frame(stream: &mut TcpStream) -> std::io::Result<Option<Vec<u8>>> {
    let mut len_buf = [0u8; 4];
    if !fill_buffer(stream, &mut len_buf)? {
        return Ok(None);
    }
    let len = u32::from_be_bytes(len_buf) as usize;

    if len > 32 * 1024 * 1024 {
        return Err(std::io::Error::new(ErrorKind::InvalidData, "frame length implausibly large"));
    }

    let mut payload = vec![0u8; len];
    stream.set_read_timeout(None)?;
    stream.read_exact(&mut payload)?;
    stream.set_read_timeout(Some(Duration::from_millis(500)))?;
    Ok(Some(payload))
}

fn fill_buffer(stream: &mut TcpStream, buf: &mut [u8]) -> std::io::Result<bool> {
    let mut filled = 0;
    while filled < buf.len() {
        match stream.read(&mut buf[filled..]) {
            Ok(0) => return Err(std::io::Error::new(ErrorKind::UnexpectedEof, "connection closed")),
            Ok(n) => filled += n,
            Err(e) if e.kind() == ErrorKind::WouldBlock || e.kind() == ErrorKind::TimedOut => {
                if filled == 0 {
                    return Ok(false);
                }
                continue;
            }
            Err(e) => return Err(e),
        }
    }
    Ok(true)
}

fn to_decoded_frame(yuv: &openh264::decoder::DecodedYUV) -> DecodedFrame {
    let (width, height) = yuv.dimensions();
    let (y_stride, u_stride, v_stride) = yuv.strides();
    let chroma_w = (width + 1) / 2;
    let chroma_h = (height + 1) / 2;

    let mut i420 = Vec::with_capacity(width * height + 2 * chroma_w * chroma_h);

    let y = yuv.y();
    for row in 0..height {
        let start = row * y_stride;
        i420.extend_from_slice(&y[start..start + width]);
    }

    let u = yuv.u();
    for row in 0..chroma_h {
        let start = row * u_stride;
        i420.extend_from_slice(&u[start..start + chroma_w]);
    }

    let v = yuv.v();
    for row in 0..chroma_h {
        let start = row * v_stride;
        i420.extend_from_slice(&v[start..start + chroma_w]);
    }

    DecodedFrame { width: width as u32, height: height as u32, i420 }
}
