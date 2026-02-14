package com.beatrunner.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.slf4j.event.Level

fun Application.configureLogging() {
    install(DoubleReceive)
    
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"] ?: "Unknown"
            "Status: $status, Method: $httpMethod, UserAgent: $userAgent"
        }
    }

    // Custom interceptor for detailed request/response logging
    intercept(ApplicationCallPipeline.Monitoring) {
        val request = call.request
        val method = request.httpMethod.value
        val path = request.path()
        val headers = request.headers.entries().joinToString(", ") { "${it.key}=${it.value}" }
        
        println("\n=== REQUEST [$method $path] ===")
        println("Headers: $headers")
        
        try {
            // Receive body as text (DoubleReceive allows this to be read again later)
            val body = call.receiveText()
            if (body.isNotEmpty()) {
                println("Body: $body")
            }
        } catch (e: Exception) {
            println("Body: [Unable to read body: ${e.message}]")
        }
        println("==============================\n")
        
        proceed()
    }
    
    sendPipeline.intercept(ApplicationSendPipeline.Transform) {
        val status = call.response.status() ?: HttpStatusCode.OK
        
        val message = subject
        
        var bodyLog = ""
        
        if (message is io.ktor.http.content.OutgoingContent) {
            bodyLog = when (message) {
                is io.ktor.http.content.TextContent -> message.text
                is io.ktor.http.content.ByteArrayContent -> String(message.bytes())
                else -> "[${message::class.simpleName}]"
            }
        } else if (message is String) {
            bodyLog = message
        } else if (message != null) {
            bodyLog = message.toString()
        }
        
        println("\n=== RESPONSE [${status.value} ${status.description}] ===")
        if (bodyLog.isNotEmpty()) {
            println("Body: $bodyLog")
        }
        println("==============================\n")
    }
}
