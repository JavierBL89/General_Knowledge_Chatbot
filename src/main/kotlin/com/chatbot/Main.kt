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
import io.ktor.server.response.*
import java.io.File

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port) {
        install(DefaultHeaders)
        install(CallLogging)
        install(CORS) {
            anyHost() // allow any frontend (localhost:5500, etc.)
            allowHeader(HttpHeaders.ContentType)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Get) // Added GET method
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        routing {
            // Add a basic health check route
            get("/health") {
                call.respondText("Hello from Ktor on Render!")
            }

            chatRoutes()
            reportDataRoute()

            // Static file serving - simplified and fixed
            staticFiles("/", File("frontend/static")) {
                default("index.html")
            }
            staticFiles("/css", File("frontend/css"))
            staticFiles("/js", File("frontend/js")) // If you have JS too
            staticFiles("/html", File("frontend/static")) // If you have HTML files

        }
    }.start(wait = true)
}

