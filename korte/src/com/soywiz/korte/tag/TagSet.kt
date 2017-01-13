package com.soywiz.korte.tag

import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag
import com.soywiz.korte.block.BlockSet

val SetTag = Tag("set", setOf(), null) {
	val main = chunks[0]
	val tr = ExprNode.Token.tokenize(main.tag.content)
	val varname = ExprNode.parseId(tr)
	ExprNode.expect(tr, "=")
	val expr = ExprNode.parseExpr(tr)
	BlockSet(varname, expr)
}