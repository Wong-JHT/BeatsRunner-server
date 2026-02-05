package com.beatrunner.routes

import com.beatrunner.common.models.CreateWorkoutRequest
import com.beatrunner.services.WorkoutService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Workout session management routes. */
fun Route.workoutRoutes() {
    val workoutService = WorkoutService()

    authenticate("auth-jwt") {
        route("/workout") {
            // Create a new workout session
            post("/session") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid token"))
                    return@post
                }

                try {
                    val request = call.receive<CreateWorkoutRequest>()
                    val session = workoutService.createWorkoutSession(userId, request)

                    if (session != null) {
                        call.respond(HttpStatusCode.Created, session)
                    } else {
                        call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("message" to "Failed to create workout session")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("message" to "Invalid request: ${e.message}")
                    )
                }
            }

            // Get workout sessions with pagination
            get("/sessions") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid token"))
                    return@get
                }

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

                // Validate pagination parameters
                if (page < 1 || pageSize < 1 || pageSize > 100) {
                    call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("message" to "Invalid pagination parameters")
                    )
                    return@get
                }

                val pagedResponse = workoutService.getWorkoutSessions(userId, page, pageSize)
                call.respond(HttpStatusCode.OK, pagedResponse)
            }

            // Get a single workout session by ID
            get("/session/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid token"))
                    return@get
                }

                val sessionId = call.parameters["id"]
                if (sessionId == null) {
                    call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("message" to "Session ID required")
                    )
                    return@get
                }

                val session = workoutService.getWorkoutSession(userId, sessionId)
                if (session != null) {
                    call.respond(HttpStatusCode.OK, session)
                } else {
                    call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("message" to "Workout session not found")
                    )
                }
            }
        }
    }
}
