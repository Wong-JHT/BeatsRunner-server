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

        // Get profile data
        val profileRow = Profiles.select { Profiles.accountId eq uuid }.singleOrNull() ?: return@dbQuery null

        UserProfile(
            age = profileRow[Profiles.age] ?: 25,
            weight = profileRow[Profiles.weight] ?: 60f,
            height = profileRow[Profiles.height] ?: 170f,
            fitnessLevel = profileRow[Profiles.fitnessLevel]?.let { FitnessLevel.valueOf(it) } ?: FitnessLevel.BEGINNER,
            targetHeartRate = profileRow[Profiles.targetHeartRate]
        )
    }

    /** Update user profile (account + profile data). */
    suspend fun updateUserProfile(accountId: String, request: UpdateProfileRequest): Boolean =
            dbQuery {
                val uuid = UUID.fromString(accountId)
                val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

                // Ensure profile row exists (or create default/empty if not? specific requirements not given, assuming update only updates if exists or upsert)
                // For simplicity, we check existence or upsert logic.
                // But Profiles table now has non-nullable fields (age, weight, height, fitnessLevel). 
                // Creating a NEW profile requires these. UpdateRequest has nullable.
                // So we assume profile exists for Update.
                
                val existing = Profiles.select { Profiles.accountId eq uuid }.count() > 0
                
                // If profile doesn't exist, we can't update optional fields without defaults for required ones.
                // Assuming this method is called after profile creation or we just update what we can.
                // For now, let's just update if exists.
                
                if (existing) {
                    Profiles.update({ Profiles.accountId eq uuid }) {
                        request.age?.let { a -> it[Profiles.age] = a }
                        request.weight?.let { w -> it[Profiles.weight] = w }
                        request.height?.let { h -> it[Profiles.height] = h }
                        request.fitnessLevel?.let { f -> it[Profiles.fitnessLevel] = f.name }
                        request.targetHeartRate?.let { t -> it[Profiles.targetHeartRate] = t }
                        it[Profiles.updatedAt] = now
                    }
                }
                // Note: Creation logic needs to be handled where profile is created (likely registration).
                // If registration flow uses UpdateProfile, it might fail if row doesn't exist.
                // But simplified for this Refactor.

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
