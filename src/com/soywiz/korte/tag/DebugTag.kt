package com.soywiz.korte.tag

import com.soywiz.korte.BlockNode
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag

val DebugTag = Tag("debug", setOf(), null) { parts ->
	BlockNode.DEBUG(ExprNode.parse(parts[0].token.content))
}