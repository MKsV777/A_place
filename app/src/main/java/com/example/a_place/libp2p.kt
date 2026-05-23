package com.example.a_place
import io.libp2p.core.Host
import io.libp2p.core.dsl.host
import io.libp2p.core.pubsub.Topic
import io.libp2p.core.pubsub.MessageApi
import io.libp2p.pubsub.gossip.GossipSub
import io.libp2p.pubsub.gossip.GossipSubParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

class P2PBridge {
    private var host: Host? = null
    // Create GossipSub protocol
    private val gossipSub = GossipSub(GossipSubParams.defaultBuilder().build())
    // Use a background scope for Android so we don't block the UI
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        scope.launch {
            try {
                // The correct DSL function is 'host' from io.libp2p.core.dsl
                val vHost = host {
                    network {
                        listen("/ip4/0.0.0.0/tcp/4001")
                    }
                    protocols {
                        add(gossipSub)
                    }
                }

                // start() returns a CompletableFuture. .get() waits for it to finish.
                vHost.start().get()
                host = vHost
                println("LibP2P Host started on: ${vHost.listenAddresses()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun publish(topic: String, message: String) {
        // In 1.2.x, publish is accessed through gossipSub.api
        val bytes = message.toByteArray(StandardCharsets.UTF_8)
        gossipSub.api.publish(bytes, Topic(topic))
    }

    // Functional Interface for Java compatibility
    fun interface MessageCallback {
        fun onMessage(text: String)
    }

    fun subscribe(topic: String, callback: MessageCallback) {
        // Specify : MessageApi to fix the "Unresolved reference data" error
        gossipSub.api.subscribe({ msg: MessageApi ->
            val data = msg.data

            // This part handles the Netty ByteBuf safely
            val bytes = ByteArray(data.readableBytes())
            data.getBytes(data.readerIndex(), bytes) // Use getBytes to avoid moving the index

            val text = String(bytes, StandardCharsets.UTF_8)

            callback.onMessage(text)
        }, androidx.privacysandbox.ads.adservices.topics.Topic(topic))
    }

    fun stop() {
        host?.stop()?.get()
    }
}