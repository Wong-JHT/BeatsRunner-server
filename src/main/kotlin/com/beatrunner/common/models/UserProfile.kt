package com.beatrunner.common.models

import kotlinx.serialization.Serializable

/**
 * User profile for personalized coaching
 */
@Serializable
data class UserProfile(
    val age: Int,
    val weight: Float, // kg
    val height: Float, // cm
    val fitnessLevel: FitnessLevel,
    val targetHeartRate: Int? = null
)

@Serializable
enum class FitnessLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

/**
 * Request to update user profile
 */
@Serializable
data class UpdateProfileRequest(
    val age: Int? = null,
    val weight: Float? = null,
    val height: Float? = null,
    val fitnessLevel: FitnessLevel? = null,
    val targetHeartRate: Int? = null
)

/** User device information. */
@Serializable
data class UserDevice(
        val id: String,
        val deviceName: String,
        val deviceType: String = "treadmill",
        val maxSpeedKmh: String, // Using String for BigDecimal
        val maxInclinePercent: String,
        val isDefault: Boolean = false
)

/** Request to add/update device. */
@Serializable
data class UpdateDeviceRequest(
        val deviceName: String,
        val deviceType: String = "treadmill",
        val maxSpeedKmh: Double = 20.0,
        val maxInclinePercent: Double = 15.0,
        val isDefault: Boolean = false
)
