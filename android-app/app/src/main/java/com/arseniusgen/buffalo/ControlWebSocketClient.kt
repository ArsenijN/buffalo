package com.arseniusgen.buffalo

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Control channel per SPECS.md 1.1: WebSocket, JSON messages, separate port from the video
 * channel. Handles the two v0 messages: "hello" (sent on connect) and "set_bitrate" (received).
 */
class ControlWebSocketClient(
    private val host: String,
    private val port: Int,
    private val deviceModel: String,
    private val resolution: String, // "WxH"
    private val onSetBitrate: (bps: Int) -> Unit
) {
    companion object {
        private const val TAG = "ControlWebSocketClient"
    }

    private val client = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS) // covers the ping/pong keepalive from SPECS.md
        .build()
    private var webSocket: WebSocket? = null

    fun connect() {
        val request = Request.Builder().url("ws://$host:$port").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(TAG, "control channel open")
                val hello = JSONObject().apply {
                    put("type", "hello")
                    put("device", deviceModel)
                    put("resolution", resolution)
                }
                webSocket.send(hello.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    when (json.optString("type")) {
                        "set_bitrate" -> onSetBitrate(json.getInt("value"))
                        "ping" -> webSocket.send(JSONObject().put("type", "pong").toString())
                        else -> Log.d(TAG, "unhandled control message: $text")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "bad control message: $text", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "control channel failed, will not auto-reconnect (call connect() again)", t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "control channel closed: $code $reason")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "client shutting down")
        webSocket = null
    }
}
