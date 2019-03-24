package com.soywiz.korte.util

class ListReader<T> constructor(val list: List<T>) {
    class OutOfBoundsException(val list: ListReader<*>, val pos: Int) : RuntimeException()

    var position = 0
    val size: Int get() = list.size
    val eof: Boolean get() = position >= list.size
    val hasMore: Boolean get() = position < list.size
    fun peek(): T = list.getOrNull(position) ?: throw OutOfBoundsException(this, position)
    fun skip(count: Int = 1) = this.apply { this.position += count }
    fun read(): T = peek().apply { skip(1) }
    override fun toString(): String = "ListReader($list)"
}
