package com.chatbot

import io.ktor.http.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


import java.io.File
import io.ktor.server.http.content.* // Needed for staticFiles

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(DefaultHeaders)
        install(CallLogging)
        install(CORS) {
            anyHost() // allow any frontend (localhost:5500, etc.)
            allowHeader(HttpHeaders.ContentType)
            allowMethod(HttpMethod.Post)
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        routing {
            chatRoutes()
            reportDataRoute()
            staticFiles("/", File(".")) {
                default("index.html")
            }

            staticFiles("/css", File("frontend/css"))
            staticFiles("/js", File("frontend/js")) // If you have JS too
            staticFiles("/html", File("frontend/static")) // If you have HTML files
        }
    }.start(wait = true)
}

