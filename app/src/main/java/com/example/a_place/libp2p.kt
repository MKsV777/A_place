package com.example.a_place

import io.libp2p.core.Host
import io.libp2p.core.dsl.host
import io.libp2p.core.pubsub.Topic
import io.libp2p.core.pubsub.MessageApi
import io.libp2p.pubsub.gossip.GossipSub // Correct import
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

object P2PBridge {
    private var host: Host? = null

    // In 1.2.1-RELEASE, GossipSub is initialized directly
    // without the "builders" sub-package.
    private val gossipSub = GossipSub()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        scope.launch {
            try {
                val vHost = host {
                    network {
                        listen("/ip4/0.0.0.0/tcp/4001")
                    }
                    protocols {
                        // The gossipSub protocol is added here
                        add(gossipSub)
                    }
                }

                // .get() waits for the CompletableFuture to complete
                vHost.start().get()
                host = vHost
                println("LibP2P Host started on: ${vHost.listenAddresses()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun publish(topic: String, message: String) {
        val bytes = message.toByteArray(StandardCharsets.UTF_8)
        // Use the .api property of gossipSub to publish in 1.2.x
        gossipSub.api.publish(bytes, Topic(topic))
    }

    fun interface MessageCallback {
        fun onMessage(text: String)
    }

    fun subscribe(topic: String, callback: MessageCallback) {
        // Use the .api property of gossipSub to subscribe in 1.2.x
        gossipSub.api.subscribe({ msg: MessageApi ->
            val data = msg.data
            val bytes = ByteArray(data.readableBytes())
            data.getBytes(data.readerIndex(), bytes)
            val text = String(bytes, StandardCharsets.UTF_8)

            callback.onMessage(text)
        }, Topic(topic))
    }

    fun stop() {
        host?.stop()?.get()
    }
}