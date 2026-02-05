package com.beatrunner.routes

import com.beatrunner.auth.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/** Authentication routes for user registration and login. */
fun Route.authRoutes() {
    val authService = AuthService()

    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()

            val result =
                    authService.register(
                            identityType = request.identityType,
                            identifier = request.identifier,
                            password = request.password,
                            nickname = request.nickname,
                            extraData = request.extraData
                    )

            if (result != null) {
                val (accountId, token) = result
                val profileCompleted = authService.isProfileCompleted(accountId)

                call.respond(
                        HttpStatusCode.Created,
                        AuthResponse(
                                accountId = accountId,
                                token = token,
                                profileCompleted = profileCompleted
                        )
                )
            } else {
                call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse("Identity already registered or invalid input")
                )
            }
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val result =
                    authService.login(
                            identityType = request.identityType,
                            identifier = request.identifier,
                            password = request.password
                    )

            if (result != null) {
                val (accountId, token) = result
                val profileCompleted = authService.isProfileCompleted(accountId)

                call.respond(
                        HttpStatusCode.OK,
                        AuthResponse(
                                accountId = accountId,
                                token = token,
                                profileCompleted = profileCompleted
                        )
                )
            } else {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials"))
            }
        }
    }

    // Account deletion requires authentication
    authenticate("auth-jwt") {
        delete("/auth/account") {
            val principal = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))
                return@delete
            }

            val userService = com.beatrunner.services.UserService()
            val success = userService.deleteUserAccount(userId)

            if (success) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Account deleted successfully"))
            } else {
                call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Failed to delete account")
                )
            }
        }
    }
}

@Serializable
data class RegisterRequest(
        val identityType: String, // email, phone, apple, wechat
        val identifier: String, // 邮箱、手机号、Apple Sub、OpenID
        val password: String? = null,
        val nickname: String? = null,
        val extraData: String? = null // JSON for third-party auth
)

@Serializable
data class LoginRequest(
        val identityType: String,
        val identifier: String,
        val password: String? = null
)

@Serializable
data class AuthResponse(
        val accountId: String,
        val token: String,
        val profileCompleted: Boolean = false
)

@Serializable data class ErrorResponse(val message: String)
