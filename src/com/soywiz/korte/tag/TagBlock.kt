package com.soywiz.korte.tag

import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag
import com.soywiz.korte.block.BlockBlock

val TagBlock = Tag("block", setOf(), "end") { context, parts ->
	val part = parts.first()
	val tokens = ExprNode.Token.tokenize(part.token.content)
	val name = ExprNode.parseId(tokens)
	if (name.isEmpty()) throw IllegalArgumentException("block without name")
	context.template.addBlock(name, part.body)
	BlockBlock(name)
}