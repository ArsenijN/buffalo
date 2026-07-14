//! Video channel client: plain TCP, `[4-byte big-endian length][H264 Annex-B access unit]`
//! repeated per frame (SPECS.md 1.1). Runs on its own OS thread (not async — this is a hot
//! byte-shuffling loop, no benefit from an executor here) and hands decoded YUV frames to a
//! callback that pushes them into OBS.

use std::io::{ErrorKind, Read};
use std::net::TcpStream;
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

pub struct TcpVideoClient {
    running: Arc<AtomicBool>,
    handle: Option<thread::JoinHandle<()>>,
}

impl TcpVideoClient {
    pub fn start(host: String, port: u16, on_frame: impl Fn(DecodedFrame) + Send + 'static) -> Self {
        let running = Arc::new(AtomicBool::new(true));
        let running_thread = running.clone();

        let handle = thread::spawn(move || {
            run(&host, port, &running_thread, on_frame);
        });

        Self { running, handle: Some(handle) }
    }

    pub fn stop(&mut self) {
        self.running.store(false, Ordering::SeqCst);
        if let Some(h) = self.handle.take() {
            let _ = h.join();
        }
    }
}

impl Drop for TcpVideoClient {
    fn drop(&mut self) {
        self.stop();
    }
}

fn run(host: &str, port: u16, running: &AtomicBool, on_frame: impl Fn(DecodedFrame)) {
    while running.load(Ordering::SeqCst) {
        match TcpStream::connect((host, port)) {
            Ok(mut stream) => {
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
                            // access_unit may contain multiple NAL units (SPS+PPS+slice on
                            // keyframes) -- split and feed each to the decoder.
                            for nal in nal_units(&access_unit) {
                                match decoder.decode(nal) {
                                    Ok(Some(yuv)) => on_frame(to_decoded_frame(&yuv)),
                                    Ok(None) => {} // decoder buffering, no picture yet
                                    Err(e) => eprintln!("[buffalo] decode error: {e}"),
                                }
                            }
                        }
                        Ok(None) => continue, // read timeout, loop to re-check `running`
                        Err(e) => {
                            eprintln!("[buffalo] video connection lost: {e}");
                            break;
                        }
                    }
                }
            }
            Err(e) => {
                eprintln!("[buffalo] connect to {host}:{port} failed: {e}");
            }
        }

        if running.load(Ordering::SeqCst) {
            thread::sleep(Duration::from_secs(1));
        }
    }
}

/// Reads one length-prefixed frame. Returns Ok(None) on a read timeout (not an error --
/// just "nothing arrived in this window, try again").
fn read_frame(stream: &mut TcpStream) -> std::io::Result<Option<Vec<u8>>> {
    let mut len_buf = [0u8; 4];
    if let Err(e) = read_exact_or_timeout(stream, &mut len_buf) {
        return match e.kind() {
            ErrorKind::WouldBlock | ErrorKind::TimedOut => Ok(None),
            _ => Err(e),
        };
    }
    let len = u32::from_be_bytes(len_buf) as usize;

    // Sanity cap: nothing in this protocol should ever produce a multi-hundred-MB access
    // unit. This guards against desyncing on garbage data and trying to allocate forever.
    if len > 32 * 1024 * 1024 {
        return Err(std::io::Error::new(ErrorKind::InvalidData, "frame length implausibly large"));
    }

    let mut payload = vec![0u8; len];
    stream.read_exact(&mut payload)?;
    Ok(Some(payload))
}

fn read_exact_or_timeout(stream: &mut TcpStream, buf: &mut [u8]) -> std::io::Result<()> {
    stream.read_exact(buf)
}

fn to_decoded_frame(yuv: &openh264::decoder::DecodedYUV) -> DecodedFrame {
    let (width, height) = yuv.dimensions();
    let mut i420 = Vec::with_capacity((width * height * 3 / 2) as usize);
    i420.extend_from_slice(yuv.y_with_stride());
    i420.extend_from_slice(yuv.u_with_stride());
    i420.extend_from_slice(yuv.v_with_stride());
    DecodedFrame { width: width as u32, height: height as u32, i420 }
}
