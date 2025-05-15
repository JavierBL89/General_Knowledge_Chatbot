package com.chatbot

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@kotlinx.serialization.Serializable
data class ChatRequest(val message: String)


// call.receive ---> function in Ktor that is used to receive and deserialize the body of an incoming HTTP request.
//                   It reads the request body and converts it into the specified type.
// call.respond ---> function in Ktor that is used to send a response back to the client.
fun Routing.chatRoutes() {
    post("/chat") {
        try {

            // Automatically parse the incoming JSON to ChatRequest
            val body = call.receive<ChatRequest>()
            val message = body.message

            val zephyr7bBeta = Zephyr7bBeta()
            val zephyr7bBetaReply = zephyr7bBeta.getModelResponseFromHFSpace(message)
            val formattedModelResponse = formatModelResponse(zephyr7bBetaReply)
            // Format the model response
            val finalFormattedReply = zephyr7bBeta.responseFormatWithLines(formattedModelResponse)

            call.respond(
                mapOf("reply" to finalFormattedReply)
            )

        } catch (e: Exception) {
            println("❌ Server Error: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, mapOf("reply" to "Error: Could not connect to server."))
        }
        //call.respondText("""{"reply":"$zephyrReply"}""", ContentType.Application.Json)
    }
}

/***
 * Function to format the model response
 * @param zephyrReply The response from the model
 * @return The formatted response
 */
fun formatModelResponse(zephy7bReply: String): String {

    val botReply = zephy7bReply.substringAfter("Bot:").trim()

    return if ("Bot:" in zephy7bReply) {
        botReply
    } else {
        zephy7bReply.trim() // fallback to raw response if format wasn't followed
    }
}



/**
 * Function to extract the message field from the JSON string
 * @param json The JSON string
 * @return The extracted message field or "Hi" if not found
 */
fun extractMessageField(json: String): String {
    return Regex("""\"message\"\s*:\s*\"([^\"]+)\"""")
        .find(json)
        ?.groupValues?.get(1)
        ?: "Hi"
}