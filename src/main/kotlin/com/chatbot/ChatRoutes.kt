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
        //val FlanT5Reply = FlanT5.getModelResponse(message)
        val Llama7bReply = Llama7b.getModelResponse(message)
        call.respondText("""{"reply":"$reply"}""", ContentType.Application.Json)
    }
}

fun extractMessageField(json: String): String {
    // Naive extractor for {"message":"..."}
    return Regex("""\"message\"\s*:\s*\"([^\"]+)\"""")
        .find(json)
        ?.groupValues?.get(1)
        ?: "Hi"
}