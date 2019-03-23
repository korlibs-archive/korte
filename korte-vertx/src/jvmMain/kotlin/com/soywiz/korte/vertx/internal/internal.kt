package com.soywiz.korte.vertx.internal

import io.vertx.core.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

internal fun <T> Handler<AsyncResult<T>>.handle(coroutineContext: CoroutineContext, callback: suspend () -> T) =
    CoroutineScope(coroutineContext).async(coroutineContext) {
        var result: T? = null
        var cause: Throwable? = null
        try {
            result = callback()
        } catch (e: Throwable) {
            cause = e
        }
        this@handle.handle(VxAsyncResult(result, cause))
    }


internal class VxAsyncResult<T>(val result: T?, val exception: Throwable?) : AsyncResult<T> {
    override fun succeeded(): Boolean = exception == null
    override fun failed(): Boolean = exception != null
    override fun result(): T = result!!
    override fun cause(): Throwable = exception!!
}

internal class DeferredHandler<T> : Handler<AsyncResult<T>> {
    val deferred = CompletableDeferred<T>()
    override fun handle(event: AsyncResult<T>) {
        if (event.failed()) {
            deferred.completeExceptionally(event.cause())
        } else {
            deferred.complete(event.result())
        }
    }
}

internal suspend fun <T> vx(callback: (Handler<AsyncResult<T>>) -> Unit): T =
    DeferredHandler<T>().also(callback).deferred.await()
