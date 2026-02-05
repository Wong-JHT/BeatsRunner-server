package com.beatrunner.routes

import com.beatrunner.ai.DeepSeekService
import com.beatrunner.common.models.MusicData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * AI analysis routes for generating treadmill commands.
 * All routes require JWT authentication.
 */
fun Route.aiRoutes() {
    val deepSeekService = DeepSeekService()

    authenticate("auth-jwt") {
        route("/ai") {
            post("/analyze") {
                try {
                    val musicData = call.receive<MusicData>()
                    val command = deepSeekService.analyzeMusicAndGenerateCommand(musicData)
                    call.respond(HttpStatusCode.OK, command)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Failed to analyze music: ${e.message}")
                    )
                }
            }
        }
    }
}
