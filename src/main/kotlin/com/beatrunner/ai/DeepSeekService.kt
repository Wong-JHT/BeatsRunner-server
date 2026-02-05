package com.beatrunner.ai

import com.beatrunner.common.models.MusicData
import com.beatrunner.common.models.TreadmillCommand
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * DeepSeek AI service for generating treadmill commands based on music data.
 * SECURITY: API key is loaded from environment variables, prompts are hardcoded.
 */
class DeepSeekService {
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    private val apiKey = dotenv["DEEPSEEK_API_KEY"] 
        ?: throw IllegalStateException("DEEPSEEK_API_KEY environment variable not set")

    // HARDCODED SYSTEM PROMPT - Cannot be overridden by client
    private val systemPrompt = """
        You are an expert running coach with deep knowledge of exercise physiology and music tempo.
        Analyze the provided music data and generate personalized treadmill recommendations.
        
        Consider the following:
        - BPM (beats per minute): Higher BPM songs should suggest faster speeds
        - Genre: Rock/EDM suggests higher intensity, Classical/Jazz suggests moderate pace
        - Lyrics: Motivational lyrics warrant encouraging messages
        
        Respond ONLY with a JSON object in this exact format:
        {
          "speed": <number between 3.0 and 15.0 km/h>,
          "incline": <number between 0.0 and 15.0 percent>,
          "coachMessage": "<motivational message 20-50 words>"
        }
        
        Do not include any other text outside the JSON object.
    """.trimIndent()

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Analyze music data and generate treadmill command.
     * @param musicData The music information to analyze
     * @return TreadmillCommand with AI-generated recommendations
     */
    suspend fun analyzeMusicAndGenerateCommand(musicData: MusicData): TreadmillCommand {
        return try {
            val userMessage = """
                Analyze this music:
                - Title: ${musicData.title}
                - Artist: ${musicData.artist}
                - BPM: ${musicData.bpm}
                - Genre: ${musicData.genre}
                ${musicData.lyricsSnippet?.let { "- Lyrics: $it" } ?: ""}
                
                Generate treadmill recommendations.
            """.trimIndent()

            val response = client.post("https://api.deepseek.com/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(DeepSeekRequest(
                    model = "deepseek-chat",
                    messages = listOf(
                        Message(role = "system", content = systemPrompt),
                        Message(role = "user", content = userMessage)
                    ),
                    temperature = 0.7,
                    maxTokens = 300
                ))
            }

            val deepSeekResponse = response.body<DeepSeekResponse>()
            val aiContent = deepSeekResponse.choices.firstOrNull()?.message?.content
                ?: throw Exception("No response from AI")

            parseAIResponse(aiContent)
        } catch (e: Exception) {
            // Fallback response on error
            generateFallbackCommand(musicData)
        }
    }

    /**
     * Parse AI response JSON into TreadmillCommand.
     */
    private fun parseAIResponse(content: String): TreadmillCommand {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            // Extract JSON from response (in case AI adds extra text)
            val jsonStart = content.indexOf('{')
            val jsonEnd = content.lastIndexOf('}') + 1
            val jsonString = content.substring(jsonStart, jsonEnd)
            
            val jsonObject = json.parseToJsonElement(jsonString) as JsonObject
            TreadmillCommand(
                speed = jsonObject["speed"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 6.0,
                incline = jsonObject["incline"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 2.0,
                coachMessage = jsonObject["coachMessage"]?.jsonPrimitive?.content 
                    ?: "Keep up the great work!"
            )
        } catch (e: Exception) {
            throw Exception("Failed to parse AI response: ${e.message}")
        }
    }

    /**
     * Generate fallback command when AI service is unavailable.
     */
    private fun generateFallbackCommand(musicData: MusicData): TreadmillCommand {
        // Simple BPM-based calculation
        val speed = when {
            musicData.bpm < 100 -> 5.0
            musicData.bpm < 120 -> 7.0
            musicData.bpm < 140 -> 9.0
            else -> 11.0
        }

        return TreadmillCommand(
            speed = speed,
            incline = 2.0,
            coachMessage = "Great choice! Let's match the energy of ${musicData.title}!"
        )
    }
}

// DeepSeek API models
@Serializable
private data class DeepSeekRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double,
    val maxTokens: Int? = null
)

@Serializable
private data class Message(
    val role: String,
    val content: String
)

@Serializable
private data class DeepSeekResponse(
    val choices: List<Choice>
)

@Serializable
private data class Choice(
    val message: Message
)
