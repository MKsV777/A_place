package com.example.a_place

import android.util.Base64
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

object IpfsRpcBridge {
    // The default RPC port for IPFS in Termux
    private const val API_BASE = "http://127.0.0.1:5001/api/v0"

    // OkHttp client configured with a very long timeout for the subscription stream
    private val client = OkHttpClient.Builder()
        .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
        .build()

    fun publish(topic: String, message: String) {
        val url = "$API_BASE/pubsub/pub".toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("arg", topic)
            ?.addQueryParameter("arg", message)
            ?.build() ?: return

        // IPFS RPC requires POST requests, even if the body is empty
        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(null, ByteArray(0)))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("❌ Failed to publish: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                println("✅ Published to $topic. Status: ${response.code}")
                response.close()
            }
        })
    }

    fun interface MessageCallback {
        fun onMessage(text: String, senderId: String)
    }

    fun subscribe(topic: String, callback: MessageCallback) {
        val url = "$API_BASE/pubsub/sub".toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("arg", topic)
            ?.build() ?: return

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(null, ByteArray(0)))
            .build()

        // We run this in a background thread because it blocks endlessly while waiting for messages
        thread {
            try {
                val response = client.newCall(request).execute()
                val source = response.body?.source()

                println("✅ Subscribed to $topic. Listening for messages...")

                // Keep reading the stream as long as Termux IPFS is running
                while (source != null && !source.exhausted()) {
                    val jsonLine = source.readUtf8Line()
                    if (jsonLine != null && jsonLine.isNotEmpty()) {

                        // IPFS returns JSON: {"from":"<peerID>", "data":"<base64_data>", ...}
                        val json = JSONObject(jsonLine)

                        // The message is Base64 encoded by IPFS, we must decode it
                        val base64Data = json.getString("data")
                        val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                        val text = String(decodedBytes, Charsets.UTF_8)

                        val senderId = json.getString("from")

                        println("📩 Received: $text")
                        callback.onMessage(text, senderId)
                    }
                }
            } catch (e: Exception) {
                println("❌ Subscription dropped: ${e.message}")
            }
        }
    }
}