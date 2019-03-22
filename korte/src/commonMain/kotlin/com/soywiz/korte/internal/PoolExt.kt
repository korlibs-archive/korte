package com.soywiz.korte.internal

import com.soywiz.kds.*

internal inline fun <T, R> Pool<T>.alloc2(callback: (T) -> R): R {
    val temp = alloc()
    try {
        return callback(temp)
    } finally {
        free(temp)
    }
}
