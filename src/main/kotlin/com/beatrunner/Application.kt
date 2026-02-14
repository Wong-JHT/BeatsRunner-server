package com.beatrunner

import com.beatrunner.auth.JwtConfig
import com.beatrunner.database.DatabaseFactory
import com.beatrunner.routes.aiRoutes
import com.beatrunner.routes.authRoutes
import com.beatrunner.routes.userRoutes
import com.beatrunner.routes.webSocketRoutes
import com.beatrunner.routes.workoutRoutes
import com.beatrunner.plugins.configureLogging
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import java.time.Duration

fun main() {
    val dotenv = dotenv { ignoreIfMissing = true }

    val port = dotenv["SERVER_PORT"]?.toIntOrNull() ?: 8080
    val host = dotenv["SERVER_HOST"] ?: "0.0.0.0"

    embeddedServer(CIO, port = port, host = host, module = Application::module).start(wait = true)
}

fun Application.module() {
    // Initialize database
    DatabaseFactory.init()

    configureLogging()

    // Install plugins
    install(ContentNegotiation) { json() }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(
                    text = "500: ${cause.message}",
                    status = HttpStatusCode.InternalServerError
            )
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm
            verifier(
                    com.auth0
                            .jwt
                            .JWT
                            .require(
                                    com.auth0.jwt.algorithms.Algorithm.HMAC256(
                                            io.github.cdimascio.dotenv.dotenv {
                                                ignoreIfMissing = true
                                            }["JWT_SECRET"]
                                                    ?: "default-secret-change-in-production"
                                    )
                            )
                            .withAudience(JwtConfig.audience)
                            .withIssuer(JwtConfig.issuer)
                            .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(JwtConfig.audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    // Register routes
    routing {
        get("/") { call.respondText("BeatRunner Server is running!") }

        get("/health") {
            call.respond(
                    mapOf(
                            "status" to "healthy",
                            "service" to "beatrunner-server",
                            "version" to "1.0.0"
                    )
            )
        }

        authRoutes()
        userRoutes()
        aiRoutes()
        workoutRoutes()
        webSocketRoutes()
    }

    println("✅ BeatRunner Server started successfully!")
    println("🌐 Server running at http://0.0.0.0:8080")
}
