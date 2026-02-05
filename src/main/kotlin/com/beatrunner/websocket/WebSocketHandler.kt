package com.beatrunner.websocket

import io.ktor.websocket.*
import kotlinx.coroutines.channels.SendChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * WebSocket connection manager for maintaining active connections.
 */
object WebSocketHandler {
    private val connections = ConcurrentHashMap<String, DefaultWebSocketSession>()

    /**
     * Register a new WebSocket connection for a user.
     */
    fun addConnection(userId: String, session: DefaultWebSocketSession) {
        connections[userId] = session
    }

    /**
     * Remove a WebSocket connection for a user.
     */
    fun removeConnection(userId: String) {
        connections.remove(userId)
    }

    /**
     * Get the WebSocket session for a user.
     */
    fun getConnection(userId: String): DefaultWebSocketSession? {
        return connections[userId]
    }

    /**
     * Send a message to a specific user.
     */
    suspend fun sendToUser(userId: String, message: String) {
        connections[userId]?.send(Frame.Text(message))
    }

    /**
     * Get count of active connections.
     */
    fun getConnectionCount(): Int = connections.size

    /**
     * Check if user is connected.
     */
    fun isUserConnected(userId: String): Boolean = connections.containsKey(userId)
}
