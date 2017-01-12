package com.soywiz.korte.tag

import com.soywiz.korte.BlockNode
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag

val ForTag = Tag("for", setOf("else"), "end") { parts ->
	val main = parts[0]
	val elseTag = parts.getOrNull(1)?.body
	val tr = ExprNode.Token.tokenize(main.token.content)
	val varname = ExprNode.parseId(tr)
	ExprNode.expect(tr, "in")
	val expr = ExprNode.parseExpr(tr)
	BlockNode.FOR(varname, expr, main.body, elseTag)
}