package com.beatrunner.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/** WorkoutMusics table schema. Stores music played during workout sessions. */
object WorkoutMusics : Table("workout_musics") {
    val id = uuid("id").autoGenerate()
    val workoutSessionId = uuid("workout_session_id").references(WorkoutSessions.id)
    val title = varchar("title", 255)
    val artist = varchar("artist", 255)
    val bpm = integer("bpm")
    val genre = varchar("genre", 100)
    val playedAt = datetime("played_at")
    val durationSeconds = integer("duration_seconds")

    override val primaryKey = PrimaryKey(id)
}
