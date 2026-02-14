package com.beatrunner.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/** WorkoutSessions table schema. Stores user workout session data. */
object WorkoutSessions : Table("workout_sessions") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(Accounts.id)
    val startTime = timestamp("start_time")
    val endTime = timestamp("end_time")
    val durationSeconds = integer("duration_seconds")
    val distanceMeters = double("distance_meters")
    val caloriesBurned = double("calories_burned")
    val avgSpeed = double("avg_speed")
    val maxSpeed = double("max_speed")
    val avgIncline = double("avg_incline")
    val avgHeartRate = integer("avg_heart_rate").nullable()
    val maxHeartRate = integer("max_heart_rate").nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
