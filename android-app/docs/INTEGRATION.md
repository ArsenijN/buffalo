# Wiring these into your existing Camera2Video-based app

Four new files, all under the same package as EncoderWrapper:

- `StreamEncoder.kt` — drop-in swap for EncoderWrapper when you're streaming instead of
  recording to a file. Same `getInputSurface()` pattern, so it plugs into your existing
  Pipeline/SoftwarePipeline `createRecordRequest()` / `getRecordTargets()` calls unchanged —
  just construct a `StreamEncoder(...)` instead of `EncoderWrapper(...)` and pass its
  `getInputSurface()` the same way.
- `TcpFrameSender.kt` — the real wire-protocol path (SPECS.md). Feed it from
  `StreamEncoder`'s callback:
  ```kotlin
  val sender = TcpFrameSender(host = "192.168.1.50", port = 5757)
  sender.start()
  val encoder = StreamEncoder(width, height, bitRate, frameRate) { data, isKeyFrame, _ ->
      sender.offer(data, isKeyFrame)
  }
  ```
- `ControlWebSocketClient.kt` — the WS control channel. `onSetBitrate` should call
  `encoder.setBitrate(bps)`. Needs OkHttp:
  `implementation("com.squareup.okhttp3:okhttp:4.12.0")` in `app/build.gradle`, plus
  `org.json` is already bundled in Android so no extra dep for that.
- `DebugMjpegServer.kt` — the debug-only "view it in a browser" path from your ask. Gate it
  behind a settings toggle (default OFF). It needs its own periodic JPEG source; cheapest way
  to get one without touching your H264 path is a second `ImageReader` with
  `ImageFormat.JPEG` added as an extra concurrent target on the same capture request you
  already build in `createRecordRequest()` — Camera2 is fine with 3 concurrent surfaces
  (preview + H264 encoder + JPEG reader). Throttle it (e.g. only take every Nth
  `onImageAvailable` callback) since this is a debug aid, not the product:
  ```kotlin
  jpegReader.setOnImageAvailableListener({ reader ->
      val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
      val buffer = image.planes[0].buffer
      val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
      debugServer.pushJpegFrame(bytes)
      image.close()
  }, backgroundHandler)
  ```

## Suggested minimal checklist to get end-to-end working

1. Add OkHttp dependency, sync Gradle.
2. Swap `EncoderWrapper` for `StreamEncoder` in whichever Fragment currently owns it.
3. Add `TcpFrameSender`, point it at your dev machine's IP (the one running OBS) and a
   hardcoded port for now — matches the "IP:port entered manually" v0 discovery approach in
   SPECS.md 1.1.
4. Add `ControlWebSocketClient` on a second port, wire `onSetBitrate` to
   `encoder.setBitrate()`.
5. Leave `DebugMjpegServer` for last — it's genuinely optional for getting the OBS path
   working, useful mainly for sanity-checking the camera capture itself without OBS in the
   loop yet.

Once this compiles, the fastest way to sanity-check the video channel *before* the OBS plugin
exists is: `nc -l 5757 | xxd | head` on your dev machine and confirm you see the 4-byte
length prefixes followed by `00 00 00 01` NAL start codes.
