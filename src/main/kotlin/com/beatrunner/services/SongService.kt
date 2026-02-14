package com.beatrunner.services

import com.beatrunner.database.DatabaseFactory.dbQuery
import com.beatrunner.database.tables.Songs
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.slf4j.LoggerFactory
import java.util.UUID

class SongService {
    private val logger = LoggerFactory.getLogger(SongService::class.java)
    
    private val getSongBPMService = GetSongBPMService()
    private val lastFmService = LastFmService()

    suspend fun getOrFetchSong(title: String, artist: String): Song? {
        // 1. Check Database
        val cachedSong = checkCache(title, artist)
        if (cachedSong != null) {
            logger.info("Song found in cache: $title by $artist")
            return cachedSong
        }

        // 2. Fetch from External APIs
        logger.info("Song not in cache. Fetching from External APIs: $title by $artist")

        return try {
            coroutineScope {
                // val bpmDeferred = async { getSongBPMService.getBpm(title, artist) }
                val tagsDeferred = async { lastFmService.getTags(title, artist) }

                // val bpm = bpmDeferred.await()
                val bpm = 0 // Temporarily set to 0 to bypass service call
                val tags = tagsDeferred.await()

                /*
                if (bpm == null) {
                    logger.warn("Could not find BPM for $title by $artist")
                    // We can still save it if tags are found, or return null. 
                    // Requirement implies we need complete info. If BPM is key for "running", maybe we abort?
                    // Let's assume we need BPM.
                    // However, we might want to return what we have or save a partial record. 
                    // For now, if BPM is missing, we might default or fail. 
                    // Let's return null to be safe if this is for running.
                    return@coroutineScope null
                }
                */

                // 3. Save to Database
                saveSongToDb(title, artist, bpm, tags)
            }
        } catch (e: Exception) {
            logger.error("Error fetching song details: ${e.message}", e)
            null
        }
    }

    private suspend fun checkCache(title: String, artist: String): Song? {
        return dbQuery {
             Songs.select { 
                 (Songs.title eq title) and (Songs.artist eq artist) 
             }.singleOrNull()?.let {
                 val tagsString = it[Songs.tags]
                 val tagsList = if (tagsString != null) {
                     Json.decodeFromString(ListSerializer(String.serializer()), tagsString)
                 } else {
                     emptyList()
                 }

                 Song(
                     id = it[Songs.id],
                     spotifyId = it[Songs.spotifyId],
                     title = it[Songs.title],
                     artist = it[Songs.artist],
                     album = it[Songs.album],
                     imageUrl = it[Songs.imageUrl],
                     bpm = it[Songs.bpm],
                     energy = it[Songs.energy],
                     valence = it[Songs.valence],
                     durationMs = it[Songs.durationMs],
                     tags = tagsList
                 )
             }
        }
    }

    private suspend fun saveSongToDb(title: String, artist: String, bpm: Int, tags: List<String>): Song {
        return dbQuery {
            val newId = UUID.randomUUID()
            // Defaulting missing fields that came from Spotify
            val durationMs = 0L 
            val energy = 0.5f 
            val valence = 0.5f
            val album = null
            val imageUrl = null
            val spotifyId = null
            
            val tagsJson = Json.encodeToString(ListSerializer(String.serializer()), tags)

            Songs.insert {
                it[id] = newId
                it[Songs.spotifyId] = spotifyId
                it[Songs.title] = title
                it[Songs.artist] = artist
                it[Songs.album] = album
                it[Songs.imageUrl] = imageUrl
                it[Songs.bpm] = bpm
                it[Songs.energy] = energy
                it[Songs.valence] = valence
                it[Songs.durationMs] = durationMs
                it[Songs.tags] = tagsJson
            }
            
            Song(
                id = newId,
                spotifyId = spotifyId,
                title = title,
                artist = artist,
                album = album,
                imageUrl = imageUrl,
                bpm = bpm,
                energy = energy,
                valence = valence,
                durationMs = durationMs,
                tags = tags
            )
        }
    }
}

// Internal Models
@Serializable
data class Song(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val spotifyId: String?,
    val title: String,
    val artist: String,
    val album: String?,
    val imageUrl: String?,
    val bpm: Int,
    val energy: Float,
    val valence: Float,
    val durationMs: Long,
    val tags: List<String> = emptyList()
)

// Helper serializer for UUID if not already available
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
}
