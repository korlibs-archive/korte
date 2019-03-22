package com.soywiz.korte

import kotlin.test.*

open class BaseTest {

}

inline fun <reified T> expectException(message: String, callback: () -> Unit) {
    try {
        callback()
    } catch (e: Throwable) {
        if (e is T) {
            assertEquals(message, e.message)
        } else {
            throw e
        }
    }
}
