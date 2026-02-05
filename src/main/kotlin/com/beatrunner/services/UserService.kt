package com.beatrunner.services

import com.beatrunner.common.models.*
import com.beatrunner.database.DatabaseFactory.dbQuery
import com.beatrunner.database.tables.*
import java.math.BigDecimal
import java.util.UUID
import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

/**
 * User service for comprehensive profile management. Handles accounts, profiles, settings, and
 * devices.
 */
class UserService {

    private val json = Json { ignoreUnknownKeys = true }

    /** Get complete user profile by account ID. */
    suspend fun getUserProfile(accountId: String): UserProfile? = dbQuery {
        val uuid = UUID.fromString(accountId)

        // Get account info
        val accountRow =
                Accounts.select { Accounts.id eq uuid }.singleOrNull() ?: return@dbQuery null

        // Get profile data
        val profileRow = Profiles.select { Profiles.accountId eq uuid }.singleOrNull()

        // Get default device
        val deviceRow =
                UserDevices.select {
                            (UserDevices.accountId eq uuid) and (UserDevices.isDefault eq true)
                        }
                        .singleOrNull()

        UserProfile(
                accountId = accountRow[Accounts.id].toString(),
                nickname = accountRow[Accounts.nickname],
                avatarUrl = accountRow[Accounts.avatarUrl],

                // Profile data
                gender = profileRow?.get(Profiles.gender)?.toString(),
                birthday = profileRow?.get(Profiles.birthday)?.toString(),
                heightCm = profileRow?.get(Profiles.heightCm)?.toString(),
                weightKg = profileRow?.get(Profiles.weightKg)?.toString(),
                restingHeartRate = profileRow?.get(Profiles.restingHeartRate) ?: 60,
                maxHeartRate = profileRow?.get(Profiles.maxHeartRate),
                runningAbilityScore = profileRow?.get(Profiles.runningAbilityScore)?.toString()
                                ?: "30.0",
                strideLengthCm = profileRow?.get(Profiles.strideLengthCm)?.toString(),

                // Device
                defaultDevice =
                        deviceRow?.let {
                            UserDevice(
                                    id = it[UserDevices.id].toString(),
                                    deviceName = it[UserDevices.deviceName],
                                    deviceType = it[UserDevices.deviceType],
                                    maxSpeedKmh = it[UserDevices.maxSpeedKmh].toString(),
                                    maxInclinePercent =
                                            it[UserDevices.maxInclinePercent].toString(),
                                    isDefault = it[UserDevices.isDefault]
                            )
                        }
        )
    }

    /** Update user profile (account + profile data). */
    suspend fun updateUserProfile(accountId: String, request: UpdateProfileRequest): Boolean =
            dbQuery {
                val uuid = UUID.fromString(accountId)
                val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

                // Update account fields
                if (request.nickname != null || request.avatarUrl != null) {
                    Accounts.update({ Accounts.id eq uuid }) {
                        request.nickname?.let { nickname -> it[Accounts.nickname] = nickname }
                        request.avatarUrl?.let { url -> it[Accounts.avatarUrl] = url }
                        it[Accounts.updatedAt] = now
                    }
                }

                // Ensure profile row exists
                val profileExists = Profiles.select { Profiles.accountId eq uuid }.count() > 0
                if (!profileExists) {
                    Profiles.insert {
                        it[Profiles.accountId] = uuid
                        it[Profiles.updatedAt] = now
                    }
                }

                // Update profile fields
                val hasProfileUpdate =
                        request.gender != null ||
                                request.birthday != null ||
                                request.heightCm != null ||
                                request.weightKg != null ||
                                request.restingHeartRate != null ||
                                request.maxHeartRate != null ||
                                request.runningAbilityScore != null ||
                                request.strideLengthCm != null

                if (hasProfileUpdate) {
                    Profiles.update({ Profiles.accountId eq uuid }) {
                        request.gender?.let { g -> it[Profiles.gender] = g.first() }
                        request.birthday?.let { bd -> it[Profiles.birthday] = LocalDate.parse(bd) }
                        request.heightCm?.let { h -> it[Profiles.heightCm] = BigDecimal.valueOf(h) }
                        request.weightKg?.let { w -> it[Profiles.weightKg] = BigDecimal.valueOf(w) }
                        request.restingHeartRate?.let { rhr -> it[Profiles.restingHeartRate] = rhr }
                        request.maxHeartRate?.let { mhr -> it[Profiles.maxHeartRate] = mhr }
                        request.runningAbilityScore?.let { score ->
                            it[Profiles.runningAbilityScore] = BigDecimal.valueOf(score)
                        }
                        request.strideLengthCm?.let { stride ->
                            it[Profiles.strideLengthCm] = BigDecimal.valueOf(stride)
                        }
                        it[Profiles.updatedAt] = now
                    }
                }

                true
            }

    /** Get user settings. */
    suspend fun getUserSettings(accountId: String): UserSettings = dbQuery {
        val uuid = UUID.fromString(accountId)
        val row = Settings.select { Settings.accountId eq uuid }.singleOrNull()

        if (row == null) {
            // Return default settings
            UserSettings()
        } else {
            val prefsJson = row[Settings.preferences]
            json.decodeFromString<UserSettings>(prefsJson)
        }
    }

    /** Update user settings. */
    suspend fun updateUserSettings(accountId: String, settings: UserSettings): Boolean = dbQuery {
        val uuid = UUID.fromString(accountId)
        val prefsJson = json.encodeToString(UserSettings.serializer(), settings)

        val exists = Settings.select { Settings.accountId eq uuid }.count() > 0

        if (exists) {
            Settings.update({ Settings.accountId eq uuid }) { it[Settings.preferences] = prefsJson }
        } else {
            Settings.insert {
                it[Settings.accountId] = uuid
                it[Settings.preferences] = prefsJson
            }
        }

        true
    }

    /** Delete user account and all associated data (cascading). */
    suspend fun deleteUserAccount(accountId: String): Boolean = dbQuery {
        val uuid = UUID.fromString(accountId)

        // Get all workout session IDs for this account
        val workoutSessionIds =
                WorkoutSessions.slice(WorkoutSessions.id)
                        .select { WorkoutSessions.userId eq uuid }
                        .map { it[WorkoutSessions.id] }

        // Delete workout musics for these sessions
        if (workoutSessionIds.isNotEmpty()) {
            WorkoutMusics.deleteWhere { WorkoutMusics.workoutSessionId inList workoutSessionIds }
        }

        // Delete workout sessions
        WorkoutSessions.deleteWhere { WorkoutSessions.userId eq uuid }

        // Delete user devices
        UserDevices.deleteWhere { UserDevices.accountId eq uuid }

        // Delete settings
        Settings.deleteWhere { Settings.accountId eq uuid }

        // Delete profile
        Profiles.deleteWhere { Profiles.accountId eq uuid }

        // Delete identities
        Identities.deleteWhere { Identities.accountId eq uuid }

        // Delete account (soft delete)
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val deleted =
                Accounts.update({ Accounts.id eq uuid }) {
                    it[Accounts.status] = "deleted"
                    it[Accounts.deletedAt] = now
                }

        deleted > 0
    }

    /** Add or update user device. */
    suspend fun addOrUpdateDevice(accountId: String, request: UpdateDeviceRequest): UserDevice? =
            dbQuery {
                val uuid = UUID.fromString(accountId)
                val deviceId = UUID.randomUUID()
                val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

                // If this device is set as default, unset other defaults
                if (request.isDefault) {
                    UserDevices.update({ UserDevices.accountId eq uuid }) {
                        it[UserDevices.isDefault] = false
                    }
                }

                UserDevices.insert {
                    it[UserDevices.id] = deviceId
                    it[UserDevices.accountId] = uuid
                    it[UserDevices.deviceName] = request.deviceName
                    it[UserDevices.deviceType] = request.deviceType
                    it[UserDevices.maxSpeedKmh] = BigDecimal.valueOf(request.maxSpeedKmh)
                    it[UserDevices.maxInclinePercent] =
                            BigDecimal.valueOf(request.maxInclinePercent)
                    it[UserDevices.isDefault] = request.isDefault
                    it[UserDevices.createdAt] = now
                }

                UserDevice(
                        id = deviceId.toString(),
                        deviceName = request.deviceName,
                        deviceType = request.deviceType,
                        maxSpeedKmh = request.maxSpeedKmh.toString(),
                        maxInclinePercent = request.maxInclinePercent.toString(),
                        isDefault = request.isDefault
                )
            }

    /** Get all devices for user. */
    suspend fun getUserDevices(accountId: String): List<UserDevice> = dbQuery {
        val uuid = UUID.fromString(accountId)

        UserDevices.select { UserDevices.accountId eq uuid }.map {
            UserDevice(
                    id = it[UserDevices.id].toString(),
                    deviceName = it[UserDevices.deviceName],
                    deviceType = it[UserDevices.deviceType],
                    maxSpeedKmh = it[UserDevices.maxSpeedKmh].toString(),
                    maxInclinePercent = it[UserDevices.maxInclinePercent].toString(),
                    isDefault = it[UserDevices.isDefault]
            )
        }
    }

    /** Delete device. */
    suspend fun deleteDevice(accountId: String, deviceId: String): Boolean = dbQuery {
        val accountUuid = UUID.fromString(accountId)
        val deviceUuid = UUID.fromString(deviceId)

        val deleted =
                UserDevices.deleteWhere {
                    (UserDevices.id eq deviceUuid) and (UserDevices.accountId eq accountUuid)
                }

        deleted > 0
    }
}
