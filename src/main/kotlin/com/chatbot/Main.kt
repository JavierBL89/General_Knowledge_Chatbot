package com.chatbot

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.defaultheaders.*
import io.netty.handler.codec.DefaultHeaders
import java.io.File
import io.ktor.server.http.content.* // Needed for staticFiles

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(DefaultHeaders)
        install(CallLogging)
        routing {
            chatRoutes()
            staticFiles("/", File("frontend/html")) {
                default("index.html")
            }

            staticFiles("/css", File("frontend/css"))
            staticFiles("/js", File("frontend/js")) // If you have JS too
        }
    }.start(wait = true)
}

