package com.beatrunner.common.models

import kotlinx.serialization.Serializable

/** Workout session data. */
@Serializable
data class WorkoutSession(
        val id: String,
        val startTime: String,
        val endTime: String,
        val durationSeconds: Int,
        val distanceMeters: Double,
        val caloriesBurned: Double,
        val avgSpeed: Double,
        val maxSpeed: Double,
        val avgHeartRate: Int?,
        val maxHeartRate: Int?,
        val musics: List<WorkoutMusic> = emptyList()
)

/** Request to create a workout session. */
@Serializable
data class CreateWorkoutRequest(
        val startTime: String,
        val endTime: String,
        val durationSeconds: Int,
        val distanceMeters: Double,
        val caloriesBurned: Double,
        val avgSpeed: Double,
        val maxSpeed: Double,
        val avgHeartRate: Int? = null,
        val maxHeartRate: Int? = null,
        val musics: List<WorkoutMusicData> = emptyList()
)

/** Music data in workout request. */
@Serializable
data class WorkoutMusicData(
        val title: String,
        val artist: String,
        val bpm: Int,
        val genre: String,
        val playedAt: String,
        val durationSeconds: Int
)

/** Music information in workout response. */
@Serializable
data class WorkoutMusic(
        val title: String,
        val artist: String,
        val bpm: Int,
        val genre: String,
        val playedAt: String,
        val durationSeconds: Int
)
