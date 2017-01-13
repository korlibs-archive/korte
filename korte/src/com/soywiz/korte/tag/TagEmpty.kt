package com.soywiz.korte.tag

import com.soywiz.korte.Block
import com.soywiz.korte.Tag

val TagEmpty = Tag("", setOf(""), null) {
	Block.group(chunks.map { it.body })
}