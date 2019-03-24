package com.soywiz.korte.internal

import com.soywiz.korte.util.*
import kotlin.coroutines.*

internal class AsyncCache {
	@PublishedApi
	internal val promises = LinkedHashMap<String, KorteDeferred<*>>()

	fun invalidateAll() {
		promises.clear()
	}

	@Suppress("UNCHECKED_CAST")
	suspend operator fun <T> invoke(key: String, gen: suspend () -> T): T =
		(promises.getOrPut(key) { KorteDeferred.asyncImmediately(coroutineContext) { gen() } } as KorteDeferred<T>).await()
}
