package com.beatrunner.database.tables

import org.jetbrains.exposed.sql.Table

/** WorkoutDataPoints table schema. Stores per-second workout data. */
object WorkoutDataPoints : Table("workout_data_points") {
    val id = uuid("id").autoGenerate()
    val workoutSessionId = uuid("workout_session_id").references(WorkoutSessions.id)
    val offsetSeconds = integer("offset_seconds")
    val speed = double("speed")
    val incline = double("incline")
    val heartRate = integer("heart_rate").nullable()

    override val primaryKey = PrimaryKey(id)
}
