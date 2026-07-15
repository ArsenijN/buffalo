/*
 * buffalo -- v0 streaming encoder.
 *
 * Replaces EncoderWrapper's "encode to file" behavior with "encode and hand raw Annex-B
 * access units to a callback" -- see SPECS.md 1.1/1.2. Deliberately keeps the same method
 * names EncoderWrapper exposes to its callers (getInputSurface/start/frameAvailable/
 * waitForFirstFrame/shutdown) so Pipeline, HardwarePipeline, SoftwarePipeline, and
 * PreviewFragment's capture-session wiring don't need to change beyond swapping the type.
 *
 * Notably: unlike EncoderWrapper's EncoderThread (which pulls output only when frameAvailable()
 * is called, via MediaCodec.dequeueOutputBuffer polling), this class uses MediaCodec's async
 * Callback API and pulls output as soon as MediaCodec has it. That makes frameAvailable() here
 * a no-op kept only for call-site compatibility with HardwarePipeline/PreviewFragment, which
 * both call it once per frame.
 */
package com.arseniusgen.buffalo

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Bundle
import android.os.ConditionVariable
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer

class StreamEncoder(
    width: Int,
    height: Int,
    bitRate: Int,
    frameRate: Int,
    private val onAccessUnit: (buffer: ByteArray, isKeyFrame: Boolean, presentationTimeUs: Long) -> Unit
) {
    companion object {
        private const val TAG = "StreamEncoder"
        private const val IFRAME_INTERVAL = 1 // one keyframe/sec, per SPECS.md 1.1
        private const val MIME = MediaFormat.MIMETYPE_VIDEO_AVC
    }

    private val codec: MediaCodec = MediaCodec.createEncoderByType(MIME)
    private var spsPpsBytes: ByteArray? = null
    private val cvFirstFrame = ConditionVariable(false)

    init {
        val format = MediaFormat.createVideoFormat(MIME, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL)
            setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline)
        }

        codec.setCallback(object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                // Fed via input Surface (Camera2/GL writes into it directly) -- nothing to queue.
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                val outBuffer: ByteBuffer? = codec.getOutputBuffer(index)
                if (outBuffer == null) {
                    codec.releaseOutputBuffer(index, false)
                    return
                }

                outBuffer.position(info.offset)
                outBuffer.limit(info.offset + info.size)

                if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    val csd = ByteArray(info.size)
                    outBuffer.get(csd)
                    spsPpsBytes = csd
                    codec.releaseOutputBuffer(index, false)
                    return
                }

                if (info.size > 0) {
                    val isKeyFrame = info.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0
                    val payload: ByteArray
                    if (isKeyFrame && spsPpsBytes != null) {
                        val sps = spsPpsBytes!!
                        payload = ByteArray(sps.size + info.size)
                        System.arraycopy(sps, 0, payload, 0, sps.size)
                        outBuffer.get(payload, sps.size, info.size)
                    } else {
                        payload = ByteArray(info.size)
                        outBuffer.get(payload)
                    }
                    onAccessUnit(payload, isKeyFrame, info.presentationTimeUs)
                    cvFirstFrame.open()
                }

                codec.releaseOutputBuffer(index, false)
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Log.e(TAG, "encoder error", e)
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                Log.d(TAG, "encoder output format changed: $format")
            }
        })

        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    fun getInputSurface(): Surface = codec.createInputSurface()

    fun start() = codec.start()

    /** No-op: kept so HardwarePipeline/PreviewFragment's existing per-frame calls compile
     *  unchanged. Real draining happens automatically via the async MediaCodec.Callback above. */
    fun frameAvailable() { /* intentionally empty, see class doc */ }

    fun waitForFirstFrame() = cvFirstFrame.block()

    /** Matches EncoderWrapper.shutdown()'s Boolean-returning signature so PreviewFragment's
     *  `if (encoder.shutdown()) { ... }` branch keeps compiling. Returns true on clean stop. */
    fun shutdown(): Boolean {
        return try {
            codec.stop()
            codec.release()
            true
        } catch (e: IllegalStateException) {
            Log.w(TAG, "shutdown() called in bad state", e)
            false
        }
    }

    /** Live bitrate change -- backs the control channel's "set_bitrate" message. */
    fun setBitrate(bps: Int) {
        codec.setParameters(Bundle().apply { putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, bps) })
    }

    fun requestKeyFrame() {
        codec.setParameters(Bundle().apply { putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0) })
    }
}
