package com.beatrunner.common.models

import kotlinx.serialization.Serializable

/**
 * AI-generated treadmill command sent to the app.
 * Contains recommended speed, incline, and motivational message.
 */
@Serializable
data class TreadmillCommand(
    val speed: Double,        // km/h
    val incline: Double,      // percentage
    val coachMessage: String  // motivational message from AI coach
)
