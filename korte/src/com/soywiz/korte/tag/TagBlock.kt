package com.soywiz.korte.tag

import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag
import com.soywiz.korte.block.BlockBlock

val TagBlock = Tag("block", setOf(), setOf("end", "endblock")) {
	val part = chunks.first()
	val tokens = ExprNode.Token.tokenize(part.tag.content)
	val name = ExprNode.parseId(tokens)
	if (name.isEmpty()) throw IllegalArgumentException("block without name")
	context.template.addBlock(name, part.body)
	BlockBlock(name)
}