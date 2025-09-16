package com.chatbot

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import utils.PromptTestBridge
import java.io.File

@kotlinx.serialization.Serializable
data class ChatRequest(val message: String)

@kotlinx.serialization.Serializable
data class ChatResponse(
    val reply: String,
    val evalGenerated: Boolean
)

fun Route.chatRoutes() {
    // Add debugging route to see if routes are working
    get("/api/test") {
        call.respondText("API routes are working!")
    }

    // Add OPTIONS for CORS preflight
    options("/chat") {
        call.respond(HttpStatusCode.OK)
    }

    post("/chat") {
        println("🔍 POST /chat endpoint hit")
        try {
            // Log the content type
            val contentType = call.request.contentType()
            println("📋 Content-Type: $contentType")

            // Log raw body for debugging
            val rawBody = call.receiveText()
            println("📝 Raw request body: $rawBody")

            // Parse JSON manually first
            val body = try {
                kotlinx.serialization.json.Json.decodeFromString<ChatRequest>(rawBody)
            } catch (e: Exception) {
                println("❌ JSON parsing error: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, mapOf("reply" to "Invalid JSON format"))
                return@post
            }

            val message = body.message
            println("💬 User message: $message")

            // Simple test response first
            val testResponse = "Hello! I received your message: '$message'"

            // Comment out the complex logic for now to test basic functionality
            /*
            val zephyr7bBeta = Llama3ChatClient()
            val lama_response = zephyr7bBeta.getModelResponse(message)

            val evaluator = PromptTestBridge()
            val evalResultCode = evaluator.testUserInput(message, lama_response)
            val evaluationPassed = (evalResultCode == 0)
            */

            val response = ChatResponse(
                reply = testResponse,
                evalGenerated = false // Set to false for testing
            )

            println("✅ Sending response: $response")
            call.respond(response)

        } catch (e: Exception) {
            println("❌ Server Error in /chat: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("reply" to "Error: Could not connect to server. Details: ${e.message}")
            )
        }
    }
}

fun Route.reportDataRoute() {
    get("/api/report-data") {
        println("🔍 GET /api/report-data endpoint hit")
        try {
            val reportFile = File("frontend/static/reportData.json")
            println("📁 Looking for report file at: ${reportFile.absolutePath}")
            println("📁 File exists: ${reportFile.exists()}")

            if (!reportFile.exists()) {
                // Create a dummy report for testing
                val dummyReport = """
                {
                    "prompt": "Test prompt",
                    "output": "Test output", 
                    "assertions": [
                        ["✅", 1.0, "test", "Test question", "Test reason"]
                    ]
                }
                """.trimIndent()
                call.respondText(dummyReport, ContentType.Application.Json)
                return@get
            }

            val fileContent = reportFile.readText(Charsets.UTF_8)
            call.respondText(fileContent, ContentType.Application.Json)
        } catch (e: Exception) {
            println("❌ Error in /api/report-data: ${e.message}")
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Failed to read report data: ${e.message}")
        }
    }
}