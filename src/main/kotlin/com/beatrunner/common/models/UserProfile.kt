package com.beatrunner.common.models

import kotlinx.serialization.Serializable

/** Complete user profile including account info and physiological data. */
@Serializable
data class UserProfile(
        val accountId: String,
        val nickname: String?,
        val avatarUrl: String?,

        // 生理数据
        val gender: String?, // M, F, O
        val birthday: String?, // YYYY-MM-DD
        val heightCm: String?, // Using String to handle BigDecimal serialization
        val weightKg: String?,

        // 运动能力
        val restingHeartRate: Int = 60,
        val maxHeartRate: Int?, // 为空时前端按 220-age 计算显示
        val runningAbilityScore: String = "30.0", // Using String for BigDecimal
        val strideLengthCm: String?, // 为空时前端按身高*0.415估算显示

        // 设备信息
        val defaultDevice: UserDevice? = null
)

/** Request to update user profile. */
@Serializable
data class UpdateProfileRequest(
        val nickname: String? = null,
        val avatarUrl: String? = null,
        val gender: String? = null,
        val birthday: String? = null, // YYYY-MM-DD
        val heightCm: Double? = null,
        val weightKg: Double? = null,
        val restingHeartRate: Int? = null,
        val maxHeartRate: Int? = null,
        val runningAbilityScore: Double? = null,
        val strideLengthCm: Double? = null
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
