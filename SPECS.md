# Specifications and development infos

This file contains step-by-step guide of how to build a buffalo from ground up

# 1. Bare bones

Bare bones buffalo will work as testing thing, meaning the bare video feed from camera from buffalo app to the OBS's simple buffalo plugin that will just have simple settings maybe and that's all. Wi-Fi/Ethernet first (real IP-level TCP, no adb dependency); USB support added later as its own transport.

## 1.1 Wire protocol (v0)

Two separate connections, kept dumb on purpose:

**Video channel — plain TCP socket**
- Framing: `[4-byte big-endian length][H264 Annex-B access unit]`, repeated per frame
- Encoder: H264 baseline profile, no B-frames, SPS/PPS repeated before every IDR (so a client attaching mid-stream can still decode)
- No RTP, no container format — raw framed NALs only, for this milestone
- Port: TBD (avoid 4747, DroidCam already uses it, to prevent confusion during side-by-side testing)

**Control channel — WebSocket, JSON messages**
- On connect, phone sends: `{"type":"hello","device":"<model>","resolution":"<WxH>"}`
- OBS plugin can send: `{"type":"set_bitrate","value":<bps>}` (first real control message to implement, doubles as bitrate-change test)
- `{"type":"ping"}` / `{"type":"pong"}` for keepalive
- Separate port from the video channel

**Discovery:** none yet. IP:port entered manually in the app for this milestone. mDNS/broadcast discovery is a post-bare-bones feature.

## 1.2 Milestone checklist — Android app

- [ ] CameraX pipeline, preview only, no encoding yet
- [ ] MediaCodec configured for H264 baseline, fed via CameraX surface output
- [ ] Pull encoded buffers from `MediaCodec.Callback.onOutputBufferAvailable`
- [ ] Open TCP socket to hardcoded IP:port, write length-prefixed frames
- [ ] Minimal status UI: "waiting for connection" / "streaming"
- [ ] WebSocket client for control channel, handle `set_bitrate` by reconfiguring the encoder without a full restart

## 1.3 Milestone checklist — OBS plugin

- [ ] Scaffold from `obs-plugintemplate` (CMake build already set up)
- [ ] Register `obs_source_info` with `OBS_SOURCE_TYPE_INPUT | OBS_SOURCE_ASYNC_VIDEO`
- [ ] TCP client that connects to the phone's video port, reassembles length-prefixed frames
- [ ] Decode NALs via `libavcodec` (bundled with OBS, no new dependency)
- [ ] Convert decoded `AVFrame` to `obs_source_frame` (NV12/I420), push via `obs_source_output_video2`
- [ ] Basic settings UI: IP/port fields, connect button
- [ ] WebSocket client for control channel, wire up a "change bitrate" button to send `set_bitrate`

Once both checklists are done and a live feed with a working bitrate change works end to end, that's the bare-bones milestone complete.

# 2. Proof of Concept

PoC buffalo will start to develop the simple communication between the OBS and phone to change the camera settings






# Other things

## Comunications

The communications between the devices should be made via WebSocket or simple HTTP requests with JSON responces, with understandable endpoints/requests names