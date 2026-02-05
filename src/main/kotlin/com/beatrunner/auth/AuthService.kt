package com.beatrunner.auth

import com.beatrunner.database.DatabaseFactory.dbQuery
import com.beatrunner.database.tables.Accounts
import com.beatrunner.database.tables.Identities
import com.beatrunner.database.tables.Profiles
import java.util.*
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt

/**
 * Authentication service for user registration and login. Supports multiple identity types: email,
 * phone, apple, wechat
 */
class AuthService {

    /**
     * Register a new user with the specified identity type.
     * @return Pair of accountId and JWT token, or null if registration fails
     */
    suspend fun register(
            identityType: String, // email, phone, apple, wechat
            identifier: String, // 邮箱地址、手机号、Apple Sub ID、微信 OpenID
            password: String? = null,
            nickname: String? = null,
            extraData: String? = null // JSON for third-party auth
    ): Pair<String, String>? = dbQuery {
        // Check if identity already exists
        val existing =
                Identities.select {
                            (Identities.identityType eq identityType) and
                                    (Identities.identifier eq identifier)
                        }
                        .singleOrNull()

        if (existing != null) {
            return@dbQuery null // Identity already registered
        }

        // Validate password for email/phone types
        if ((identityType == "email" || identityType == "phone") && password.isNullOrBlank()) {
            return@dbQuery null // Password required
        }

        // Create account
        val accountId = UUID.randomUUID()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        Accounts.insert {
            it[id] = accountId
            it[Accounts.nickname] = nickname
            it[status] = "active"
            it[createdAt] = now
            it[updatedAt] = now
        }

        // Initialize empty profile (will be filled by user later)
        Profiles.insert {
            it[Profiles.accountId] = accountId
            it[updatedAt] = now
        }

        // Create identity
        val hashedPassword =
                if (password != null) BCrypt.hashpw(password, BCrypt.gensalt()) else null

        Identities.insert {
            it[Identities.accountId] = accountId
            it[Identities.identityType] = identityType
            it[Identities.identifier] = identifier
            it[credential] = hashedPassword
            it[Identities.extraData] = extraData
            it[createdAt] = now
        }

        val token = JwtConfig.generateToken(accountId.toString())
        Pair(accountId.toString(), token)
    }

    /**
     * Login with existing credentials.
     * @return Pair of accountId and JWT token, or null if login fails
     */
    suspend fun login(
            identityType: String,
            identifier: String,
            password: String? = null
    ): Pair<String, String>? = dbQuery {
        val identityRow =
                Identities.select {
                            (Identities.identityType eq identityType) and
                                    (Identities.identifier eq identifier)
                        }
                        .singleOrNull()
                        ?: return@dbQuery null

        // Verify password for email/phone types
        if (identityType == "email" || identityType == "phone") {
            val storedHash = identityRow[Identities.credential] ?: return@dbQuery null
            if (password == null || !BCrypt.checkpw(password, storedHash)) {
                return@dbQuery null // Invalid password
            }
        }

        // Update last login time
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        Identities.update({ Identities.id eq identityRow[Identities.id] }) { it[lastLoginAt] = now }

        val accountId = identityRow[Identities.accountId].toString()
        val token = JwtConfig.generateToken(accountId)
        Pair(accountId, token)
    }

    /** Check if account has completed profile setup. */
    suspend fun isProfileCompleted(accountId: String): Boolean = dbQuery {
        val uuid = UUID.fromString(accountId)
        val profile = Profiles.select { Profiles.accountId eq uuid }.singleOrNull()

        // Profile is considered complete if at least gender and birthday are set
        profile != null && profile[Profiles.gender] != null && profile[Profiles.birthday] != null
    }

    /** Get user ID from JWT token. */
    fun getUserIdFromToken(token: String): String? {
        return JwtConfig.verifyToken(token)
    }
}
