package com.soywiz.korte.internal

import com.soywiz.korte.util.*
import kotlin.coroutines.*

internal class AsyncCache {
	@PublishedApi
	internal val deferreds = LinkedHashMap<String, KorteDeferred<*>>()

	fun invalidateAll() {
		deferreds.clear()
	}

	@Suppress("UNCHECKED_CAST")
	suspend operator fun <T> invoke(key: String, gen: suspend () -> T): T {
		val deferred = (deferreds.getOrPut(key) { KorteDeferred.asyncImmediately(coroutineContext) { gen() } } as KorteDeferred<T>)
		return deferred.await()
	}
}
