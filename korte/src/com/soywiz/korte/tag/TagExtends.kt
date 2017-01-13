package com.soywiz.korte.tag

import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag
import com.soywiz.korte.block.BlockExtends

val TagExtends = Tag("extends", setOf(), null) {
	val part = chunks.first()
	val parent = ExprNode.parseExpr(ExprNode.Token.tokenize(part.tag.content))
	BlockExtends(parent)
}