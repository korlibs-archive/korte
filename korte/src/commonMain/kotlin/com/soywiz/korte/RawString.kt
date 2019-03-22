package com.soywiz.korte

import com.soywiz.korio.util.htmlspecialchars
import com.soywiz.korte.dynamic.*

class RawString(val str: String) {
	override fun toString(): String = str
}

fun Any?.toEscapedString(): String = when (this) {
	is RawString -> this.str
	//else -> DynamicContext { this.toDynamicString().htmlspecialchars() }
	else -> Dynamic2.toString(this).htmlspecialchars()
}
