package com.soywiz.korte.vertx.util

import io.vertx.core.*
import kotlinx.coroutines.*

class VxAsyncResult<T>(val result: T?, val exception: Throwable?) : AsyncResult<T> {
    override fun succeeded(): Boolean = exception == null
    override fun failed(): Boolean = exception != null
    override fun result(): T = result!!
    override fun cause(): Throwable = exception!!
}

class DeferredHandler<T> : Handler<AsyncResult<T>> {
    val deferred = CompletableDeferred<T>()
    override fun handle(event: AsyncResult<T>) {
        if (event.failed()) {
            deferred.completeExceptionally(event.cause())
        } else {
            deferred.complete(event.result())
        }
    }
}

suspend fun <T> vx(callback: (Handler<AsyncResult<T>>) -> Unit): T =
    DeferredHandler<T>().also(callback).deferred.await()
