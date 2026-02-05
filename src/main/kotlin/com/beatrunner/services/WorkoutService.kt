package com.beatrunner.services

import com.beatrunner.common.models.*
import com.beatrunner.database.DatabaseFactory.dbQuery
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
            request: CreateWorkoutRequest
    ): WorkoutSession? = dbQuery {
        val userUuid = UUID.fromString(userId)
        val sessionId = UUID.randomUUID()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        // Parse datetime strings
        val startTime = LocalDateTime.parse(request.startTime)
        val endTime = LocalDateTime.parse(request.endTime)

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
            it[avgHeartRate] = request.avgHeartRate
            it[maxHeartRate] = request.maxHeartRate
            it[createdAt] = now
        }

        // Insert music records
        val musics =
                request.musics.map { music ->
                    val playedAt = LocalDateTime.parse(music.playedAt)
                    WorkoutMusics.insert {
                        it[workoutSessionId] = sessionId
                        it[title] = music.title
                        it[artist] = music.artist
                        it[bpm] = music.bpm
                        it[genre] = music.genre
                        it[WorkoutMusics.playedAt] = playedAt
                        it[durationSeconds] = music.durationSeconds
                    }

                    WorkoutMusic(
                            title = music.title,
                            artist = music.artist,
                            bpm = music.bpm,
                            genre = music.genre,
                            playedAt = music.playedAt,
                            durationSeconds = music.durationSeconds
                    )
                }

        WorkoutSession(
                id = sessionId.toString(),
                startTime = request.startTime,
                endTime = request.endTime,
                durationSeconds = request.durationSeconds,
                distanceMeters = request.distanceMeters,
                caloriesBurned = request.caloriesBurned,
                avgSpeed = request.avgSpeed,
                maxSpeed = request.maxSpeed,
                avgHeartRate = request.avgHeartRate,
                maxHeartRate = request.maxHeartRate,
                musics = musics
        )
    }

    /** Get workout sessions for a user with pagination. */
    suspend fun getWorkoutSessions(
            userId: String,
            page: Int,
            pageSize: Int
    ): PagedResponse<WorkoutSession> = dbQuery {
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
                                                WorkoutMusic(
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

                            WorkoutSession(
                                    id = sessionId.toString(),
                                    startTime = row[WorkoutSessions.startTime].toString(),
                                    endTime = row[WorkoutSessions.endTime].toString(),
                                    durationSeconds = row[WorkoutSessions.durationSeconds],
                                    distanceMeters = row[WorkoutSessions.distanceMeters],
                                    caloriesBurned = row[WorkoutSessions.caloriesBurned],
                                    avgSpeed = row[WorkoutSessions.avgSpeed],
                                    maxSpeed = row[WorkoutSessions.maxSpeed],
                                    avgHeartRate = row[WorkoutSessions.avgHeartRate],
                                    maxHeartRate = row[WorkoutSessions.maxHeartRate],
                                    musics = musics
                            )
                        }

        val totalPages = ((totalCount + pageSize - 1) / pageSize).toInt()

        PagedResponse(
                data = sessions,
                page = page,
                pageSize = pageSize,
                totalCount = totalCount,
                totalPages = totalPages
        )
    }

    /** Get a single workout session by ID. */
    suspend fun getWorkoutSession(userId: String, sessionId: String): WorkoutSession? = dbQuery {
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
                                        WorkoutMusic(
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

                    WorkoutSession(
                            id = row[WorkoutSessions.id].toString(),
                            startTime = row[WorkoutSessions.startTime].toString(),
                            endTime = row[WorkoutSessions.endTime].toString(),
                            durationSeconds = row[WorkoutSessions.durationSeconds],
                            distanceMeters = row[WorkoutSessions.distanceMeters],
                            caloriesBurned = row[WorkoutSessions.caloriesBurned],
                            avgSpeed = row[WorkoutSessions.avgSpeed],
                            maxSpeed = row[WorkoutSessions.maxSpeed],
                            avgHeartRate = row[WorkoutSessions.avgHeartRate],
                            maxHeartRate = row[WorkoutSessions.maxHeartRate],
                            musics = musics
                    )
                }
    }
}
