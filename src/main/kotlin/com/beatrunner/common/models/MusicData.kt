package com.beatrunner.common.models

import kotlinx.serialization.Serializable

/**
 * Music data sent from the app to the backend for AI analysis.
 * Contains information about the currently playing song.
 */
@Serializable
data class MusicData(
    val title: String,
    val artist: String,
    val bpm: Int,
    val genre: String,
    val lyricsSnippet: String? = null
)
