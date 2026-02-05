package com.beatrunner.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/** WorkoutSessions table schema. Stores user workout session data. */
object WorkoutSessions : Table("workout_sessions") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(Accounts.id)
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")
    val durationSeconds = integer("duration_seconds")
    val distanceMeters = double("distance_meters")
    val caloriesBurned = double("calories_burned")
    val avgSpeed = double("avg_speed")
    val maxSpeed = double("max_speed")
    val avgHeartRate = integer("avg_heart_rate").nullable()
    val maxHeartRate = integer("max_heart_rate").nullable()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
