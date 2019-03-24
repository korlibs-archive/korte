package com.soywiz.korte.internal

internal class Deque<T> {
	private val array = arrayListOf<T>()

	val size: Int get() = array.size
	val length: Int get() = array.size
	fun isEmpty() = array.isEmpty()
	fun isNotEmpty() = array.isNotEmpty()
	val last get() = array.lastOrNull()
	fun removeLast() {
		array.removeAt(array.size - 1)
	}
}