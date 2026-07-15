package com.arseniusgen.buffalo

import android.util.Log
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Sends encoded access units to the OBS plugin over the plain TCP video channel described in
 * SPECS.md 1.1:
 *   [4-byte big-endian length][H264 Annex-B access unit], repeated per frame.
 *
 * Buffering is RAM-only (ArrayBlockingQueue), never spills to flash. If the queue fills up
 * (e.g. Wi-Fi hiccup), we drop the OLDEST frame rather than blocking the encoder thread or
 * growing unbounded — dropping is cheap to recover from because the next keyframe resyncs the
 * decoder; blocking is not.
 */
class TcpFrameSender(
    private val host: String,
    private val port: Int,
    queueCapacity: Int = 60, // ~2s of buffer at 30fps; tune once real latency numbers exist
    private val onConnectionStateChanged: (connected: Boolean) -> Unit = {}
) {
    companion object {
        private const val TAG = "TcpFrameSender"
    }

    private data class Frame(val data: ByteArray, val isKeyFrame: Boolean)

    private val queue = ArrayBlockingQueue<Frame>(queueCapacity)
    private val running = AtomicBoolean(false)
    private var senderThread: Thread? = null

    fun start() {
        if (!running.compareAndSet(false, true)) return
        senderThread = Thread(::runLoop, "TcpFrameSender").apply { start() }
    }

    fun stop() {
        running.set(false)
        senderThread?.interrupt()
        senderThread = null
        queue.clear()
    }

    /** Call this from StreamEncoder's onAccessUnit callback. Never blocks the caller. */
    fun offer(data: ByteArray, isKeyFrame: Boolean) {
        if (!running.get()) return
        val frame = Frame(data, isKeyFrame)
        if (!queue.offer(frame)) {
            queue.poll() // drop oldest
            queue.offer(frame)
            Log.w(TAG, "queue full, dropped oldest frame")
        }
    }

    private fun runLoop() {
        while (running.get()) {
            var socket: Socket? = null
            try {
                socket = Socket()
                socket.tcpNoDelay = true // latency over throughput for a live feed
                socket.connect(InetSocketAddress(host, port), 3000)
                onConnectionStateChanged(true)
                Log.d(TAG, "connected to $host:$port")

                val out = socket.getOutputStream()
                while (running.get()) {
                    val frame = queue.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS) ?: continue
                    writeFrame(out, frame.data)
                }
            } catch (e: Exception) {
                if (running.get()) {
                    Log.w(TAG, "connection lost, retrying in 1s: ${e.message}")
                }
            } finally {
                onConnectionStateChanged(false)
                try { socket?.close() } catch (_: Exception) {}
            }

            if (running.get()) {
                try { Thread.sleep(1000) } catch (_: InterruptedException) { return }
            }
        }
    }

    private fun writeFrame(out: OutputStream, data: ByteArray) {
        val header = byteArrayOf(
            (data.size ushr 24 and 0xFF).toByte(),
            (data.size ushr 16 and 0xFF).toByte(),
            (data.size ushr 8 and 0xFF).toByte(),
            (data.size and 0xFF).toByte()
        )
        out.write(header)
        out.write(data)
        out.flush()
    }
}
