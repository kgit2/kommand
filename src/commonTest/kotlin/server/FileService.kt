package server

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

suspend fun startServer(port: Int, responseText: String): CIOApplicationEngine {
    val engine = embeddedServer(CIO, port = port) {
        routing {
            get("/") {
                call.respondText(responseText)
            }
        }
    }
    engine.start()
    return engine
}
