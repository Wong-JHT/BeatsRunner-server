package com.beatrunner.database.tables

import org.jetbrains.exposed.sql.Table

/**
 * Songs table schema. Stores song metadata and audio features.
 * Caches data from Spotify to avoid repeated API calls.
 */
object Songs : Table("songs") {
    val id = uuid("id").autoGenerate()
    val spotifyId = varchar("spotify_id", 255).nullable().uniqueIndex()
    val title = varchar("title", 255)
    val artist = varchar("artist", 255)
    val album = varchar("album", 255).nullable()
    val imageUrl = varchar("image_url", 500).nullable()
    val tags = text("tags").nullable() // JSON list or comma separated strings for genres/styles
    
    // Audio Features
    val bpm = integer("bpm")
    val energy = float("energy") // 0.0 to 1.0
    val valence = float("valence") // Musical positiveness (0.0 to 1.0)
    val durationMs = long("duration_ms")

    override val primaryKey = PrimaryKey(id)
}
