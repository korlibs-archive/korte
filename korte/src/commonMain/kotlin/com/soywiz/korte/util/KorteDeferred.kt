package com.soywiz.korte.util

import kotlin.coroutines.*

class KorteDeferred<T> {
	private var result: Result<T>? = null
	private val continuations = arrayListOf<Continuation<T>>()

	fun completeWith(result: Result<T>) {
		this.result = result
		resolveIfRequired()
	}

	fun completeExceptionally(t: Throwable) = completeWith(Result.failure(t))
	fun complete(value: T) = completeWith(Result.success(value))

	suspend fun await(): T = suspendCoroutine { c ->
		continuations += c
		resolveIfRequired()
	}

	private fun resolveIfRequired() {
		val copy = continuations.toList()
		continuations.clear()
		val result = result
		if (result != null) for (v in copy) v.resumeWith(result)
	}

	fun toContinuation(coroutineContext: CoroutineContext) = object : Continuation<T> {
		override val context: CoroutineContext = coroutineContext
		override fun resumeWith(result: Result<T>) = completeWith(result)
	}

	companion object {
		fun <T> asyncImmediately(coroutineContext: CoroutineContext, callback: suspend () -> T): KorteDeferred<T> {
			val deferred = KorteDeferred<T>()
			callback.startCoroutine(object : Continuation<T> {
				override val context: CoroutineContext = coroutineContext
				override fun resumeWith(result: Result<T>) {
					if (result.isSuccess) {
						deferred.complete(result.getOrThrow())
					} else {
						deferred.completeExceptionally(result.exceptionOrNull()!!)
					}
				}
			})
			return deferred
		}
	}
}
