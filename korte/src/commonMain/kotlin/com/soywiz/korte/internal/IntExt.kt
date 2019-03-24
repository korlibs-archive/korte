package com.soywiz.korte.internal

internal infix fun Int.umod(other: Int): Int {
	val remainder = this % other
	return when {
		remainder < 0 -> remainder + other
		else -> remainder
	}
}
