package com.soywiz.korte

import com.soywiz.korio.util.htmlspecialchars
import com.soywiz.korte.dynamic.*

class RawString(val str: String) {
	override fun toString(): String = str
}

fun Any?.toEscapedString(): String {
	return if (this is RawString) {
		this.str
	} else {
		this.toDynamicString().htmlspecialchars()
	}
}
