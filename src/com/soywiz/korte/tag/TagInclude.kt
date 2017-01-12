package com.soywiz.korte.tag

import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag
import com.soywiz.korte.block.BlockInclude
import com.soywiz.korte.util.toDynamicString

val TagInclude = Tag("include", setOf(), null) {
	val part = chunks.first()
	val fileName = ExprNode.parseExpr(part.tag.tokens)
	BlockInclude(fileName)
}
