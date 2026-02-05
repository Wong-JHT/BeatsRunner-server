package com.beatrunner.common.models

import kotlinx.serialization.Serializable

/** User settings/preferences. */
@Serializable
data class UserSettings(
        val unitSystem: String = "metric", // metric, imperial
        val audioGuide: Boolean = true,
        val safetyLockSpeed: Double = 12.0,
        val warmupDuration: Int = 180, // 秒
        val musicSyncMode: String = "cadence", // cadence, heart_rate
        val privacyConsentVersion: String? = null
)

/** Request to update settings. */
@Serializable
data class UpdateSettingsRequest(
        val unitSystem: String? = null,
        val audioGuide: Boolean? = null,
        val safetyLockSpeed: Double? = null,
        val warmupDuration: Int? = null,
        val musicSyncMode: String? = null,
        val privacyConsentVersion: String? = null
)
