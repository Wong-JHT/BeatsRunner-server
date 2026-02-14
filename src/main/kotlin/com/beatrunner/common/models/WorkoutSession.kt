package com.beatrunner.common.models

import kotlinx.serialization.Serializable

/**
 * Request to create a workout session.
 */
@Serializable
data class WorkoutSessionRequest(
    val startTime: String,
    val endTime: String,
    val durationSeconds: Int,
    val distanceMeters: Double,
    val caloriesBurned: Double,
    val avgSpeed: Double,
    val maxSpeed: Double,
    val avgIncline: Double,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val musics: List<WorkoutMusicData> = emptyList(),
    val points: List<WorkoutDataPoint> = emptyList()
)

/**
 * Detailed workout session data
 */
@Serializable
data class WorkoutSessionDetail(
    val id: String,
    val startTime: String,
    val endTime: String,
    val durationSeconds: Int,
    val distanceMeters: Double,
    val caloriesBurned: Double,
    val avgSpeed: Double,
    val maxSpeed: Double,
    val avgIncline: Double,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val musics: List<WorkoutMusicData> = emptyList(),
    val points: List<WorkoutDataPoint> = emptyList()
)

/**
 * Paginated response for workout sessions
 */
@Serializable
data class WorkoutSessionsResponse(
    val data: List<WorkoutSessionDetail>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int
)

/**
 * Per-second workout data point.
 */
@Serializable
data class WorkoutDataPoint(
    val offsetSeconds: Int,
    val speed: Double,
    val incline: Double,
    val heartRate: Int? = null
)

/**
 * Music data in workout request.
 */
@Serializable
data class WorkoutMusicData(
    val title: String,
    val artist: String,
    val bpm: Int,
    val genre: String,
    val playedAt: String,
    val durationSeconds: Int
)

