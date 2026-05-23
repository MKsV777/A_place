package com.example.a_place

// 1. Correct Imports for the repositories you added
import io.libp2p.core.Host
import io.libp2p.core.dsl.host
import io.libp2p.core.pubsub.Topic
import io.libp2p.pubsub.gossip.GossipSub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture

class pubsub {
    // We use a CoroutineScope because LibP2P is asynchronous
    private val scope = CoroutineScope(Dispatchers.IO)

    // Initialize GossipSub protocol
    private val gossipSub = GossipSub()

    // Define the Topic
    private val chatTopic = Topic("general-chat")

    // Define the Host variable
    var host: Host? = null

    fun startP2P() {
        // We must start the host inside a Coroutine
        scope.launch {
            val hostBuilder = host {
                protocols {
                    add(gossipSub)
                }
                network {
                    listen("/ip4/0.0.0.0/tcp/0")
                }
            }

            // Build and Start the host
            val startedHost = hostBuilder.start().get()
            host = startedHost

            println("Host started: ${startedHost.listenAddresses()}")

            // Subscribe to messages
            gossipSub.subscribe({ msg ->
                val text = kotlin.text.String(msg.data)
                println("Received: $text")
            }, chatTopic)
        }
    }

    fun sendMessage(text: String) {
        val bytes = text.toByteArray()
        // Publish to the network
        gossipSub.publish(bytes, chatTopic)
    }
}