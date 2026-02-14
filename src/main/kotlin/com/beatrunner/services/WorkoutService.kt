package com.beatrunner.services

import com.beatrunner.common.models.*
import com.beatrunner.database.DatabaseFactory.dbQuery
import com.beatrunner.database.tables.WorkoutDataPoints
import com.beatrunner.database.tables.WorkoutMusics
import com.beatrunner.database.tables.WorkoutSessions
import java.util.UUID
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.*

/** Service for workout session management. */
class WorkoutService {

    /** Create a new workout session with music records. */
    suspend fun createWorkoutSession(
            userId: String,
            request: WorkoutSessionRequest
    ): WorkoutSessionDetail? = dbQuery {
        val userUuid = UUID.fromString(userId)
        val sessionId = UUID.randomUUID()
        val now = Clock.System.now()

        // Parse datetime strings with timezone support
        val startTime = Instant.parse(request.startTime)
        val endTime = Instant.parse(request.endTime)

        // Insert workout session
        WorkoutSessions.insert {
            it[id] = sessionId
            it[WorkoutSessions.userId] = userUuid
            it[WorkoutSessions.startTime] = startTime
            it[WorkoutSessions.endTime] = endTime
            it[durationSeconds] = request.durationSeconds
            it[distanceMeters] = request.distanceMeters
            it[caloriesBurned] = request.caloriesBurned
            it[avgSpeed] = request.avgSpeed
            it[maxSpeed] = request.maxSpeed
            it[avgIncline] = request.avgIncline
            it[avgHeartRate] = request.avgHeartRate
            it[maxHeartRate] = request.maxHeartRate
            it[createdAt] = now
        }

        // Insert music records
        val musics =
                request.musics.map { music ->
                    val playedAt = Instant.parse(music.playedAt)
                    WorkoutMusics.insert {
                        it[workoutSessionId] = sessionId
                        it[title] = music.title
                        it[artist] = music.artist
                        it[bpm] = music.bpm
                        it[genre] = music.genre
                        it[WorkoutMusics.playedAt] = playedAt
                        it[durationSeconds] = music.durationSeconds
                    }

                    music // already in WorkoutMusicData format
                }

        // Insert data points
        request.points.forEach { point ->
            WorkoutDataPoints.insert {
                it[workoutSessionId] = sessionId
                it[offsetSeconds] = point.offsetSeconds
                it[speed] = point.speed
                it[incline] = point.incline
                it[heartRate] = point.heartRate
            }
        }

        WorkoutSessionDetail(
                id = sessionId.toString(),
                startTime = request.startTime,
                endTime = request.endTime,
                durationSeconds = request.durationSeconds,
                distanceMeters = request.distanceMeters,
                caloriesBurned = request.caloriesBurned,
                avgSpeed = request.avgSpeed,
                maxSpeed = request.maxSpeed,
                avgIncline = request.avgIncline,
                avgHeartRate = request.avgHeartRate,
                maxHeartRate = request.maxHeartRate,
                musics = musics,
                points = request.points
        )
    }

    /** Get workout sessions for a user with pagination. */
    suspend fun getWorkoutSessions(
            userId: String,
            page: Int,
            pageSize: Int
    ): WorkoutSessionsResponse = dbQuery {
        val userUuid = UUID.fromString(userId)
        val offset = (page - 1) * pageSize

        // Get total count
        val totalCount = WorkoutSessions.select { WorkoutSessions.userId eq userUuid }.count()

        // Get paginated sessions
        val sessions =
                WorkoutSessions.select { WorkoutSessions.userId eq userUuid }
                        .orderBy(WorkoutSessions.startTime to SortOrder.DESC)
                        .limit(pageSize, offset.toLong())
                        .map { row ->
                            val sessionId = row[WorkoutSessions.id]

                            // Get musics for this session
                            val musics =
                                    WorkoutMusics.select {
                                        WorkoutMusics.workoutSessionId eq sessionId
                                    }
                                            .orderBy(WorkoutMusics.playedAt to SortOrder.ASC)
                                            .map { musicRow ->
                                                WorkoutMusicData(
                                                        title = musicRow[WorkoutMusics.title],
                                                        artist = musicRow[WorkoutMusics.artist],
                                                        bpm = musicRow[WorkoutMusics.bpm],
                                                        genre = musicRow[WorkoutMusics.genre],
                                                        playedAt =
                                                                musicRow[WorkoutMusics.playedAt]
                                                                        .toString(),
                                                        durationSeconds =
                                                                musicRow[
                                                                        WorkoutMusics
                                                                                .durationSeconds]
                                                )
                                            }

                            WorkoutSessionDetail(
                                    id = sessionId.toString(),
                                    startTime = row[WorkoutSessions.startTime].toString(),
                                    endTime = row[WorkoutSessions.endTime].toString(),
                                    durationSeconds = row[WorkoutSessions.durationSeconds],
                                    distanceMeters = row[WorkoutSessions.distanceMeters],
                                    caloriesBurned = row[WorkoutSessions.caloriesBurned],
                                    avgSpeed = row[WorkoutSessions.avgSpeed],
                                    maxSpeed = row[WorkoutSessions.maxSpeed],
                                    avgIncline = row[WorkoutSessions.avgIncline],
                                    avgHeartRate = row[WorkoutSessions.avgHeartRate],
                                    maxHeartRate = row[WorkoutSessions.maxHeartRate],
                                    musics = musics,
                                    points = emptyList() // Don't return points in list view
                            )
                        }

        val totalPages = ((totalCount + pageSize - 1) / pageSize).toInt()

        WorkoutSessionsResponse(
                data = sessions,
                page = page,
                pageSize = pageSize,
                totalCount = totalCount.toInt(),
                totalPages = totalPages
        )
    }

    /** Get a single workout session by ID. */
    suspend fun getWorkoutSession(userId: String, sessionId: String): WorkoutSessionDetail? = dbQuery {
        val userUuid = UUID.fromString(userId)
        val sessionUuid = UUID.fromString(sessionId)

        WorkoutSessions.select {
            (WorkoutSessions.id eq sessionUuid) and (WorkoutSessions.userId eq userUuid)
        }
                .singleOrNull()
                ?.let { row ->
                    // Get musics for this session
                    val musics =
                            WorkoutMusics.select { WorkoutMusics.workoutSessionId eq sessionUuid }
                                    .orderBy(WorkoutMusics.playedAt to SortOrder.ASC)
                                    .map { musicRow ->
                                        WorkoutMusicData(
                                                title = musicRow[WorkoutMusics.title],
                                                artist = musicRow[WorkoutMusics.artist],
                                                bpm = musicRow[WorkoutMusics.bpm],
                                                genre = musicRow[WorkoutMusics.genre],
                                                playedAt =
                                                        musicRow[WorkoutMusics.playedAt].toString(),
                                                durationSeconds =
                                                        musicRow[WorkoutMusics.durationSeconds]
                                        )
                                    }

                    // Get data points for this session
                    val points = WorkoutDataPoints.select {
                        WorkoutDataPoints.workoutSessionId eq sessionUuid
                    }.orderBy(WorkoutDataPoints.offsetSeconds to SortOrder.ASC)
                    .map { pointRow ->
                        WorkoutDataPoint(
                            offsetSeconds = pointRow[WorkoutDataPoints.offsetSeconds],
                            speed = pointRow[WorkoutDataPoints.speed],
                            incline = pointRow[WorkoutDataPoints.incline],
                            heartRate = pointRow[WorkoutDataPoints.heartRate]
                        )
                    }

                    WorkoutSessionDetail(
                            id = row[WorkoutSessions.id].toString(),
                            startTime = row[WorkoutSessions.startTime].toString(),
                            endTime = row[WorkoutSessions.endTime].toString(),
                            durationSeconds = row[WorkoutSessions.durationSeconds],
                            distanceMeters = row[WorkoutSessions.distanceMeters],
                            caloriesBurned = row[WorkoutSessions.caloriesBurned],
                            avgSpeed = row[WorkoutSessions.avgSpeed],
                            maxSpeed = row[WorkoutSessions.maxSpeed],
                            avgIncline = row[WorkoutSessions.avgIncline],
                            avgHeartRate = row[WorkoutSessions.avgHeartRate],
                            maxHeartRate = row[WorkoutSessions.maxHeartRate],
                            musics = musics,
                            points = points
                    )
                }
    }
}
