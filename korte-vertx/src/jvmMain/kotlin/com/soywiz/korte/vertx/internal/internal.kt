package com.soywiz.korte.vertx.internal

import com.soywiz.korte.vertx.util.*
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
