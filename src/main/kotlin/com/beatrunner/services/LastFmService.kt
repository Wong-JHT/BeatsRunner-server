package com.beatrunner.services

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class LastFmService {
    private val logger = LoggerFactory.getLogger(LastFmService::class.java)
    private val dotenv = dotenv { ignoreIfMissing = true }
    private val apiKey = dotenv["LAST_FM_API_KEY"]

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getTags(title: String, artist: String): List<String> {
        if (apiKey.isNullOrBlank()) {
            logger.warn("LAST_FM_API_KEY is missing. Cannot fetch tags.")
            return emptyList()
        }

        try {
            // Last.fm API: track.getTopTags
            val url = "https://ws.audioscrobbler.com/2.0/?method=track.gettoptags&artist=${artist.encodeURLParameter()}&track=${title.encodeURLParameter()}&api_key=$apiKey&format=json&autocorrect=1"
            logger.info("Fetching tags from Last.fm")

            val response = client.get(url)
            if (response.status != HttpStatusCode.OK) {
                logger.error("Last.fm API Error: ${response.status}")
                return emptyList()
            }

            val responseBody = response.body<String>()
            println("LastFm Response: $responseBody")

            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
            val result = json.decodeFromString<LastFmTrackTagsResponse>(responseBody)
            return result.toptags.tag.map { it.name }
        } catch (e: Exception) {
            logger.error("Error fetching tags from Last.fm: ${e.message}", e)
            return emptyList()
        }
    }
}

@Serializable
data class LastFmTrackTagsResponse(
    val toptags: LastFmTopTags
)

@Serializable
data class LastFmTopTags(
    val tag: List<LastFmTag> = emptyList()
)

@Serializable
data class LastFmTag(
    val name: String,
    val count: Int? = 0,
    val url: String? = null
)
