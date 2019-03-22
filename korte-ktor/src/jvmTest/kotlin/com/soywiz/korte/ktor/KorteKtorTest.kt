package com.soywiz.korte.ktor

import com.soywiz.korio.file.std.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*
import org.junit.Test
import kotlin.test.*

class KorteKtorTest {
    @Test
    fun test() {
        runBlocking {
            withTestApplication {
                application.apply {
                    install(Korte) {
                        templateRoot(
                            MemoryVfsMix(
                                "demo.tpl" to "Hello {{ hello }}"
                            )
                        )
                    }
                    routing {
                        get("/") {
                            call.respond(KorteContent("demo.tpl", mapOf("hello" to "world")))
                        }
                    }
                    assertEquals("Hello world", handleRequest(HttpMethod.Get, "/") { }.response.content)
                }
            }
        }
    }
}