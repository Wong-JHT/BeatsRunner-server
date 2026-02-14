package com.beatrunner.routes

import com.beatrunner.services.SongService
import com.beatrunner.common.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * AI analysis routes for generating treadmill commands.
 * All routes require JWT authentication.
 */
fun Route.aiRoutes() {
    val songService = SongService()

    authenticate("auth-jwt") {
        route("/ai") {
            post("/analyze") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: throw Exception("Invalid user token")

                    val request = call.receive<SongAnalysisRequest>()
                    val songInfo = request.songInfo

                    // 1. Get Song Data (DB -> APIs)
                    val song = songService.getOrFetchSong(songInfo.title, songInfo.artist)
                    
                    if (song == null) {
                         call.respond(
                            HttpStatusCode.NotFound,
                            ApiError("SONG_NOT_FOUND", "Could not find song details.")
                        )
                        return@post
                    }

                    // 2. Construct Response
                    val response = SongAnalysisResponse(
                        title = song.title,
                        artist = song.artist,
                        album = song.album,
                        imageUrl = song.imageUrl,
                        bpm = song.bpm,
                        energy = song.energy,
                        valence = song.valence,
                        durationMs = song.durationMs,
                        spotifyId = song.spotifyId,
                        tags = song.tags
                    )

                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiError("AI_ERROR", "Failed to analyze song: ${e.message}")
                    )
                }
            }
            
            // Keep /analyze-song for backward compatibility if needed, or redirect
            // For now, we replace it as per requirement.
            
            post("/command") {
                // ... (Keep existing if needed, or remove if unused)
                // For now keeping it as it was in the original file, just implementing /analyze
                 try {
                    val request = call.receive<AICommandRequest>()
                    // Placeholder for actual command generation logic
                    val response = AICommandResponse(
                        command = TreadmillCommand(
                            speed = 8.5,
                            incline = 1.0,
                            coachMessage = "Keep it up!"
                        ),
                        coachMessage = "Great pace! Let's maintain this.",
                        reasoning = "Heart rate is optimal."
                    )
                     call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                     call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiError("AI_ERROR", "Failed to generate command: ${e.message}")
                    )
                }
            }
        }
    }
}
