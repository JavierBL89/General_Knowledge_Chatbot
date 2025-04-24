package com.chatbot

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


// call.receive ---> function in Ktor that is used to receive and deserialize the body of an incoming HTTP request.
//                   It reads the request body and converts it into the specified type.
// call.respond ---> function in Ktor that is used to send a response back to the client.
fun Routing.chatRoutes() {
    post("/chat") {

        val jsonBody = call.receiveText()

        val message = extractMessageField(jsonBody)

        val zephyr7b = Zephyr7b()
        val zephyrReply = zephyr7b.getModelResponse(message)
        val formattedModelResponse = formatModelResponse(zephyrReply)
        call.respond(
            mapOf("reply" to formattedModelResponse)
        )

        //call.respondText("""{"reply":"$zephyrReply"}""", ContentType.Application.Json)
    }
}

/***
 * Function to format the model response
 * @param zephyrReply The response from the model
 * @return The formatted response
 */
fun formatModelResponse(zephyrReply: String): String {

    val botReply = zephyrReply.substringAfter("Bot:").trim()

    return if ("Bot:" in zephyrReply) {
        botReply
    } else {
        zephyrReply.trim() // fallback to raw response if format wasn't followed
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