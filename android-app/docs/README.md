# buffalo OBS plugin — v0 skeleton

## Status

- `tcp_client.rs`: real, complete. Connects to the phone, reassembles the length-prefixed
  Annex-B frames from SPECS.md 1.1, decodes with `openh264`, hands back planar I420.
- `control.rs`: real, complete. WebSocket client, handles `hello`, sends `set_bitrate`.
- `lib.rs`: source registration + settings UI is real; the very last step (pushing a decoded
  I420 frame into OBS via `obs_source_output_video2`) is a `TODO` — see the comment in
  `video_tick()`. That one call depends on exactly which `obs-wrapper` version resolves, and
  I couldn't verify it against a live OBS install from here. Everything up to that point
  (getting frames off the network and decoded) is done and doesn't depend on OBS at all, so
  it's worth building and testing standalone first (see below) before chasing that last call.

## Build requirements

- **rustc 1.83 or newer** (the `openh264` crate's build script needs it). If you're on an
  older toolchain via `apt`, switch to `rustup` — `rustup default stable` will get you
  current.
- OBS's development headers/libs available for `obs-sys` to link against — the pragmatic path
  on Ubuntu is `cargo obs-build install` per the `libobs-wrapper`/`cargo-obs-build` tooling,
  or build/install OBS Studio from source and point `obs-sys` at it. `obs-wrapper` (this
  crate's registration layer) needs *some* libobs present to link, even though `openh264`
  decode doesn't.
- `nasm` on PATH is optional but gives openh264 a real speed boost — worth having for a live
  video path, not required to build.

```bash
cargo build --release
# then copy target/release/libbuffalo_obs_plugin.so to your OBS plugins folder,
# e.g. ~/.config/obs-studio/plugins/buffalo/bin/64bit/ on Linux
```

## Testing the network+decode path without OBS at all

Since `tcp_client.rs` has no OBS dependency, you can sanity-check it in isolation before
fighting with OBS's plugin loader:

```rust
// examples/standalone_test.rs
fn main() {
    let client = buffalo_obs_plugin::tcp_client::TcpVideoClient::start(
        "192.168.1.50".into(), 5757,
        |frame| println!("got frame {}x{}, {} bytes", frame.width, frame.height, frame.i420.len()),
    );
    std::thread::sleep(std::time::Duration::from_secs(30));
    drop(client);
}
```
(You'd need to make the `tcp_client` module `pub` and add a `[[bin]]`/`examples/` entry — left
out of the main crate for now since `cdylib` crates can't normally also ship a runnable
example without a bit of Cargo.toml juggling. Ask if you want that wired up.)

## Why decode is separated from the OBS push

`video_tick()` runs on OBS's video thread at canvas frame rate. The TCP read + H264 decode
loop runs on its own dedicated thread and just deposits the latest decoded frame into a
`Mutex<Option<DecodedFrame>>`. This keeps `video_tick()` cheap (a lock + take, nothing else)
regardless of network jitter — if a phone frame is late, OBS just re-shows the last one
instead of blocking its video thread on a socket read. This is the buffering-in-RAM
constraint from SPECS.md applied on the OBS side, mirroring `TcpFrameSender`'s
`ArrayBlockingQueue` on the phone side.
