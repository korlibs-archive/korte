package com.soywiz.korte.util

class ListReader<T>(val list: List<T>) {
	var position = 0
	val size: Int get() = list.size
	val eof: Boolean get() = position >= list.size
	val hasMore: Boolean get() = position < list.size
	fun peek(): T = list[position]
	fun skip(count: Int = 1) = this.apply { this.position += count }
	fun read(): T = peek().apply { skip(1) }
	override fun toString(): String = "ListReader($list)"
}

fun <T> List<T>.reader() = ListReader(this)

fun <T> ListReader<T>.expect(value: T): T {
	val v = read()
	if (v != value) error("Expecting '$value' but found '$v'")
	return v
}

fun <T> ListReader<T>.dump() {
	println("ListReader:")
	for (item in list) {
		println(" - $item")
	}
}
