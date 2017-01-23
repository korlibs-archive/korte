package com.soywiz.korte

import com.soywiz.korio.async.await
import org.junit.Assert

open class BaseTest {
	suspend fun expectException(message: String, c: Class<Throwable> = Throwable::class.java, callback: suspend () -> Unit) {
		try {
			callback.await()
		} catch (e: Throwable) {
			if (c.isAssignableFrom(e.javaClass)) {
				Assert.assertEquals(message, e.message)
			} else {
				throw e
			}
		}
	}
}