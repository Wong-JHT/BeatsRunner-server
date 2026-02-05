package com.beatrunner.routes

import com.beatrunner.common.models.UpdateProfileRequest
import com.beatrunner.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** User profile management routes. */
fun Route.userRoutes() {
    val userService = UserService()

    authenticate("auth-jwt") {
        route("/user") {
            // Get current user profile
            get("/profile") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))
                    return@get
                }

                val profile = userService.getUserProfile(userId)
                if (profile != null) {
                    call.respond(HttpStatusCode.OK, profile)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
                }
            }

            // Update user profile
            put("/profile") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))
                    return@put
                }

                val request = call.receive<UpdateProfileRequest>()
                val success = userService.updateUserProfile(userId, request)

                if (success) {
                    val updatedProfile = userService.getUserProfile(userId)
                    call.respond(HttpStatusCode.OK, updatedProfile!!)
                } else {
                    call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse("Failed to update profile")
                    )
                }
            }
        }
    }
}
