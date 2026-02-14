package com.beatrunner.common.models

import kotlinx.serialization.Serializable

/**
 * Request to analyze song and get BPM
 */
@Serializable
data class SongAnalysisRequest(
    val songInfo: SongInfo
)

/**
 * Basic song information
 */
@Serializable
data class SongInfo(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val duration: Long,
    val packageName: String,
    val isPlaying: Boolean,
    val progress: Long,
    val lastUpdated: Long
)

/**
 * Response containing song analysis including BPM
 */
@Serializable
data class SongAnalysisResponse(
    val title: String,
    val artist: String,
    val album: String?,
    val imageUrl: String?,
    val bpm: Int,
    val energy: Float,
    val valence: Float,
    val durationMs: Long,
    val spotifyId: String?,
    val tags: List<String> = emptyList()
)

/**
 * Request for AI coaching command
 */
@Serializable
data class AICommandRequest(
    val songInfo: SongInfo,
    val currentSpeed: Float,
    val currentIncline: Float,
    val heartRate: Int? = null,
    val userProfile: UserProfile
)

/**
 * AI coaching response
 */
@Serializable
data class AICommandResponse(
    val command: TreadmillCommand,
    val coachMessage: String,
    val reasoning: String? = null
)

/**
 * API error response
 */
@Serializable
data class ApiError(
    val code: String,
    val message: String
)
