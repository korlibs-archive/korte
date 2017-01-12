package com.soywiz.korte.tag

import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag
import com.soywiz.korte.block.BlockDebug

val TagDebug = Tag("debug", setOf(), null) {
	BlockDebug(ExprNode.parse(chunks[0].tag.content))
}