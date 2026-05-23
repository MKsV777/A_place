package com.example.a_place

import io.libp2p.core.Host
import io.libp2p.core.dsl.host
import io.libp2p.pubsub.gossip.Gossip
import io.libp2p.pubsub.gossip.GossipRouter
import io.libp2p.core.pubsub.Topic
import io.libp2p.core.pubsub.MessageApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pubsub.pb.Rpc
import com.google.protobuf.ByteString
import io.netty.buffer.Unpooled
import java.nio.charset.StandardCharsets
import java.util.function.Consumer

object P2PBridge {
    private var host: Host? = null
    private var router: GossipRouter? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        scope.launch {
            try {
                val gossipRouter = Gossip()

                val vHost = host {
                    network {
                        listen("/ip4/0.0.0.0/tcp/4001")
                    }
                    protocols {
                        add(gossipRouter)
                    }
                }

                vHost.start().get()
                host = vHost
                router = gossipRouter as? GossipRouter

                println("✅ LibP2P Host started on: ${vHost.listenAddresses()}")
            } catch (e: Exception) {
                println("❌ Error starting host: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Publishes a message to a topic.
     */
    fun publish(topic: String, message: String) {
        scope.launch {
            try {
                val r = router ?: throw Exception("Router not initialized. Call start() first!")
                val bytes = message.toByteArray(StandardCharsets.UTF_8)

                // Create Rpc.Message
                val rpcMsg = Rpc.Message.newBuilder()
                    .addTopicIDs(topic)
                    .setData(ByteString.copyFrom(bytes))
                    .build()

                // Wrap in a simple object that implements publish expectations
                // Create a PubsubMessage wrapper by wrapping the data
                val pubsubMsg = object : Any() {
                    val data = Unpooled.wrappedBuffer(rpcMsg.toByteArray())
                    val topicID = topic
                }

                // Use reflection to call publish with Rpc.Message
                val publishMethod = r.javaClass.getDeclaredMethod(
                    "publish",
                    Rpc.Message::class.java
                )
                publishMethod.isAccessible = true
                publishMethod.invoke(r, rpcMsg)

                println("✅ Published to '$topic': $message")
            } catch (e: Exception) {
                println("❌ Error publishing: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun interface MessageCallback {
        fun onMessage(text: String)
    }

    /**
     * Subscribes to a topic and receives messages.
     */
    fun subscribe(topic: String, callback: MessageCallback) {
        scope.launch {
            try {
                val r = router ?: throw Exception("Router not initialized. Call start() first!")

                val messageHandler = Consumer { msg: MessageApi ->
                    try {
                        val data = msg.data
                        val bytes = ByteArray(data.readableBytes())
                        data.getBytes(data.readerIndex(), bytes)
                        val text = String(bytes, StandardCharsets.UTF_8)
                        callback.onMessage(text)
                        println("✅ Received on '$topic': $text")
                    } catch (e: Exception) {
                        println("❌ Error processing message: ${e.message}")
                        e.printStackTrace()
                    }
                }

                // Use reflection to access protected subscribe
                val subscribeMethod = r.javaClass.getDeclaredMethod(
                    "subscribe",
                    Consumer::class.java,
                    Topic::class.java
                )
                subscribeMethod.isAccessible = true
                subscribeMethod.invoke(r, messageHandler, Topic(topic))

                println("✅ Subscribed to '$topic'")
            } catch (e: Exception) {
                println("❌ Error subscribing: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        try {
            host?.stop()?.get()
            println("✅ Host stopped")
        } catch (e: Exception) {
            println("❌ Error stopping: ${e.message}")
        }
    }
}