package com.soywiz.korte.tag

import com.soywiz.korte.BlockNode
import com.soywiz.korte.Tag

val TagEmpty = Tag("", setOf(""), "") { parts ->
	BlockNode.group(parts.map { it.body })
}