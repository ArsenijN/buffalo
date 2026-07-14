package com.arseniusgen.buffalo

import android.util.Log
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Debug-only MJPEG-over-HTTP server. This is NOT the buffalo wire protocol from SPECS.md —
 * it exists purely so you (or anyone testing the app) can open http://<phone-ip>:<port>/ in a
 * plain browser and eyeball the camera feed without OBS or the real plugin involved. Wire this
 * to a settings toggle that defaults to OFF; the real path for OBS is TcpFrameSender.
 *
 * Feed it JPEG frames however you like — cheapest is a low-fps ImageReader(ImageFormat.JPEG)
 * added as an extra concurrent target on your existing capture request. Don't push full-rate
 * H264-encoder-quality frames through this; it's a debug aid, not the product.
 */
class DebugMjpegServer(private val port: Int) {
    companion object {
        private const val TAG = "DebugMjpegServer"
        private const val BOUNDARY = "buffalo-debug-boundary"
    }

    private val running = AtomicBoolean(false)
    private var serverSocket: ServerSocket? = null
    private val clients = CopyOnWriteArrayList<OutputStream>()

    fun start() {
        if (!running.compareAndSet(false, true)) return
        Thread({
            try {
                serverSocket = ServerSocket(port)
                Log.i(TAG, "debug MJPEG server listening on :$port")
                while (running.get()) {
                    val socket = serverSocket?.accept() ?: break
                    Thread({ handleClient(socket) }, "DebugMjpegClient").start()
                }
            } catch (e: Exception) {
                if (running.get()) Log.e(TAG, "server error", e)
            }
        }, "DebugMjpegServer").start()
    }

    fun stop() {
        running.set(false)
        try { serverSocket?.close() } catch (_: Exception) {}
        clients.forEach { try { it.close() } catch (_: Exception) {} }
        clients.clear()
    }

    /** Call this for every JPEG frame you want mirrored to connected browsers. */
    fun pushJpegFrame(jpeg: ByteArray) {
        if (clients.isEmpty()) return
        val header = (
            "--$BOUNDARY\r\n" +
                "Content-Type: image/jpeg\r\n" +
                "Content-Length: ${jpeg.size}\r\n\r\n"
            ).toByteArray(Charsets.US_ASCII)
        val footer = "\r\n".toByteArray(Charsets.US_ASCII)

        for (client in clients) {
            try {
                client.write(header)
                client.write(jpeg)
                client.write(footer)
                client.flush()
            } catch (e: Exception) {
                clients.remove(client)
                try { client.close() } catch (_: Exception) {}
            }
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            // We don't bother parsing the request line/headers — every path just gets the
            // stream. This is a debug tool, not a real HTTP server.
            socket.getInputStream().bufferedReader().readLine()

            val out = BufferedOutputStream(socket.getOutputStream())
            val headers = (
                "HTTP/1.0 200 OK\r\n" +
                    "Content-Type: multipart/x-mixed-replace; boundary=$BOUNDARY\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "Connection: close\r\n\r\n"
                ).toByteArray(Charsets.US_ASCII)
            out.write(headers)
            out.flush()

            clients.add(out)
            Log.i(TAG, "debug client connected: ${socket.inetAddress}")
        } catch (e: Exception) {
            Log.w(TAG, "client setup failed", e)
            try { socket.close() } catch (_: Exception) {}
        }
    }
}
