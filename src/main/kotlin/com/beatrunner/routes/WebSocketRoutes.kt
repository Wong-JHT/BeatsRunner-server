package com.beatrunner.routes

import com.beatrunner.ai.DeepSeekService
import com.beatrunner.auth.JwtConfig
import com.beatrunner.common.models.MusicData
import com.beatrunner.websocket.WebSocketHandler
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** WebSocket routes for real-time coaching communication. */
fun Route.webSocketRoutes() {
    val deepSeekService = DeepSeekService()
    val json = Json { ignoreUnknownKeys = true }

    webSocket("/ws/coach") {
        // Authenticate via JWT token in query parameter
        val token = call.request.queryParameters["token"]
        if (token == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing authentication token"))
            return@webSocket
        }

        val userId = JwtConfig.verifyToken(token)
        if (userId == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid authentication token"))
            return@webSocket
        }

        // Register connection
        WebSocketHandler.addConnection(userId, this)
        println("WebSocket connected: userId=$userId")

        try {
            // Send welcome message
            send(
                    Frame.Text(
                            json.encodeToString(
                                    mapOf(
                                            "type" to "connected",
                                            "message" to "Welcome to BeatRunner Coach!"
                                    )
                            )
                    )
            )

            // Listen for incoming messages
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val receivedText = frame.readText()

                        try {
                            val musicData = json.decodeFromString<MusicData>(receivedText)

                            // Process music data asynchronously
                            launch {
                                try {
                                    // Call AI service
                                    val command =
                                            deepSeekService.analyzeMusicAndGenerateCommand(
                                                    musicData
                                            )

                                    // Send command back to client
                                    send(Frame.Text(json.encodeToString(command)))
                                } catch (e: Exception) {
                                    send(
                                            Frame.Text(
                                                    json.encodeToString(
                                                            mapOf(
                                                                    "type" to "error",
                                                                    "message" to
                                                                            "Failed to analyze music: ${e.message}"
                                                            )
                                                    )
                                            )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            send(
                                    Frame.Text(
                                            json.encodeToString(
                                                    mapOf(
                                                            "type" to "error",
                                                            "message" to
                                                                    "Invalid message format: ${e.message}"
                                                    )
                                            )
                                    )
                            )
                        }
                    }
                    else -> {
                        // Ignore other frame types
                    }
                }
            }
        } catch (e: Exception) {
            println("WebSocket error for userId=$userId: ${e.message}")
        } finally {
            // Unregister connection
            WebSocketHandler.removeConnection(userId)
            println("WebSocket disconnected: userId=$userId")
        }
    }
}
