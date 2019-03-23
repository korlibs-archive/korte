package com.soywiz.korte.vertx.internal

import io.vertx.core.buffer.*
import io.vertx.core.http.*
import kotlinx.coroutines.*

suspend fun HttpClientRequest.readString(): String {
    val data = Buffer.buffer()
    val completed = CompletableDeferred<Unit>()
    handler {
        it.handler { data.appendBuffer(it) }
        it.endHandler { completed.complete(Unit) }
    }.exceptionHandler {
        completed.completeExceptionally(it)
    }.end()
    completed.await()
    return data.toString(Charsets.UTF_8)
}
