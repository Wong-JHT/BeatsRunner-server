package com.beatrunner.services

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class GetSongBPMService {
    private val logger = LoggerFactory.getLogger(GetSongBPMService::class.java)
    private val dotenv = dotenv { ignoreIfMissing = true }
    private val apiKey = dotenv["GET_SONG_BPM_API_KEY"]

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getBpm(title: String, artist: String): Int? {
        if (apiKey.isNullOrBlank()) {
            logger.warn("GET_SONG_BPM_API_KEY is missing. Cannot fetch BPM.")
            return null
        }

        try {
            // GetSongBPM API search
            val url = "https://api.getsongbpm.com/search/?api_key=$apiKey&type=both&lookup=${(title + " " + artist).encodeURLParameter()}"
            logger.info("Searching GetSongBPM: $url")

            val response = client.get(url)
            if (response.status != HttpStatusCode.OK) {
                logger.error("GetSongBPM API Error: ${response.status}")
                return null
            }

            val searchResult = response.body<GetSongBPMResponse>()
            val song = searchResult.search.firstOrNull { 
                it.title.equals(title, ignoreCase = true) || it.artist.name.equals(artist, ignoreCase = true) 
            } ?: searchResult.search.firstOrNull()

            return song?.tempo?.toIntOrNull()
        } catch (e: Exception) {
            logger.error("Error fetching BPM from GetSongBPM: ${e.message}", e)
            return null
        }
    }
}

@Serializable
data class GetSongBPMResponse(
    val search: List<GetSongBPMTrack> = emptyList()
)

@Serializable
data class GetSongBPMTrack(
    val id: String,
    val title: String,
    val artist: GetSongBPMArtist,
    val tempo: String
)

@Serializable
data class GetSongBPMArtist(
    val id: String,
    val name: String
)
