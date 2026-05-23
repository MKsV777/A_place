package com.example.a_place

import android.util.Base64
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

object IpfsRpcBridge {
    // la direccion de la api de ipfs rpc http
    private const val API_BASE = "http://127.0.0.1:5001/api/v0"

    // OkHttp client para un timeout muy largo
    private val client = OkHttpClient.Builder()
        .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
        .build()

    fun publicar(topic: String, message: String) {
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
                println("no se publico la cosa: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                println("se publico a $topic. la respuesta es : ${response.code}")
                response.close()
            }
        })
    }

    fun interface MessageCallback {
        fun onMessage(text: String, senderId: String)
    }

    fun subscibirse(topic: String, callback: MessageCallback) {
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

                        println("se recibio: $text")
                        callback.onMessage(text, senderId)
                    }
                }
            } catch (e: Exception) {
                println("ya no conectado a la subscripcion: ${e.message}")
            }
        }
    }
}