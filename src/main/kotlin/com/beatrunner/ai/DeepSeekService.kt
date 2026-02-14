package com.beatrunner.ai

import com.beatrunner.common.models.MusicData
import com.beatrunner.common.models.TreadmillCommand
import com.beatrunner.common.models.UserProfile
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
import kotlinx.serialization.json.jsonObject

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
    /**
     * Generate treadmill command based on user profile and song data.
     */
    suspend fun generateCommand(userProfile: UserProfile?, song: com.beatrunner.services.Song): TreadmillCommand {
        return try {
            val userContext = userProfile?.let {
                """
                User Profile:
                - Age: ${it.age ?: "Unknown"}
                - Weight: ${it.weight ?: "Unknown"}kg
                - Height: ${it.height ?: "Unknown"}cm
                - Fitness Level: ${it.fitnessLevel}
                - Target Heart Rate: ${it.targetHeartRate ?: "Unknown"}
                """.trimIndent()
            } ?: "User Profile: Unknown (Assume beginner)"

            val songContext = """
                Song Analysis:
                - Title: ${song.title}
                - Artist: ${song.artist}
                - BPM: ${song.bpm}
                - Energy: ${song.energy} (0.0-1.0)
                - Valence (Mood): ${song.valence} (0.0-1.0)
            """.trimIndent()

            val userMessage = """
                $userContext
                
                $songContext
                
                Generate a treadmill workout command that matches this song's energy and the user's profile.
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
            // Fallback
            generateFallbackCommand(song)
        }
    }

    private fun generateFallbackCommand(song: com.beatrunner.services.Song): TreadmillCommand {
        val baseSpeed = 6.0 + (song.energy * 4.0) // 6.0 to 10.0
        val speed = if (song.bpm > 130) baseSpeed + 1.0 else baseSpeed
        
        return TreadmillCommand(
            speed = speed,
            incline = if (song.energy > 0.7) 3.0 else 1.0,
            coachMessage = "Let's move to the beat of ${song.title}!"
        )
    }

    /**
     * Legacy method for backward compatibility or simple text analysis
     */
    suspend fun analyzeMusicAndGenerateCommand(musicData: MusicData): TreadmillCommand {
        // ... implementation can remain or be deprecated
        return TreadmillCommand(6.0, 1.0, "Maintain pace") 
    }


    private fun parseAIResponse(content: String): TreadmillCommand {
        return try {
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            val jsonElement = json.parseToJsonElement(content).jsonObject
            
            val speed = jsonElement["speed"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 6.0
            val incline = jsonElement["incline"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
            val coachMessage = jsonElement["coachMessage"]?.jsonPrimitive?.content ?: "Keep moving!"
            
            TreadmillCommand(speed, incline, coachMessage)
        } catch (e: Exception) {
            println("Error parsing AI response: ${e.message}")
            TreadmillCommand(6.0, 0.0, "Couldn't parse AI advice. Just run!")
        }
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
