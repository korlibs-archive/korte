package com.soywiz.korte.tag

import com.soywiz.korte.Block
import com.soywiz.korte.Tag

val TagEmpty = Tag("", setOf(""), "") { context, parts ->
	Block.group(parts.map { it.body })
}