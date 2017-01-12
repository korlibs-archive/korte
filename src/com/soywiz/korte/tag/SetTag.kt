package com.soywiz.korte.tag

import com.soywiz.korte.BlockNode
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag

val SetTag = Tag("set", setOf(), null) { parts ->
	val main = parts[0]
	val tr = ExprNode.Token.tokenize(main.token.content)
	val varname = ExprNode.parseId(tr)
	ExprNode.expect(tr, "=")
	val expr = ExprNode.parseExpr(tr)
	BlockNode.SET(varname, expr)
}