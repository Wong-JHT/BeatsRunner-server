package com.beatrunner.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import java.util.*

/**
 * JWT configuration and token generation.
 */
object JwtConfig {
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    private val secret = dotenv["JWT_SECRET"] ?: "default-secret-change-in-production"
    val issuer = dotenv["JWT_ISSUER"] ?: "beatrunner"
    val audience = dotenv["JWT_AUDIENCE"] ?: "beatrunner-users"
    val realm = dotenv["JWT_REALM"] ?: "beatrunner"
    
    private val algorithm = Algorithm.HMAC256(secret)

    /**
     * Generate a JWT token for a user.
     */
    fun generateToken(userId: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 86400000)) // 24 hours
            .sign(algorithm)
    }

    /**
     * Verify and decode a JWT token.
     */
    fun verifyToken(token: String): String? {
        return try {
            val verifier = JWT.require(algorithm)
                .withAudience(audience)
                .withIssuer(issuer)
                .build()
            val decodedJWT = verifier.verify(token)
            decodedJWT.getClaim("userId").asString()
        } catch (e: Exception) {
            null
        }
    }
}
