package com.soywiz.korte

data class Tag(val name: String, val nextList: Set<String>, val end: String?, val aliases: List<String> = listOf(), val buildNode: (parts: List<com.soywiz.korte.Tag.Part>) -> BlockNode) {
	data class Part(val token: Token.TTag, val body: BlockNode)

	companion object {
		val EMPTY = com.soywiz.korte.Tag("", setOf(""), "") { parts ->
			BlockNode.group(parts.map { it.body })
		}
		val IF = com.soywiz.korte.Tag("if", setOf("else"), "end") { parts ->
			val main = parts[0]
			val elseBlock = parts.getOrNull(1)
			BlockNode.IF(ExprNode.parse(main.token.content), main.body, elseBlock?.body)
		}
		val FOR = com.soywiz.korte.Tag("for", setOf(), "end") { parts ->
			val main = parts[0]
			val tr = ExprNode.Token.tokenize(main.token.content)
			val varname = ExprNode.parseId(tr)
			ExprNode.expect(tr, "in")
			val expr = ExprNode.parseExpr(tr)
			BlockNode.FOR(varname, expr, main.body)
		}
		val DEBUG = com.soywiz.korte.Tag("debug", setOf(), null) { parts ->
			BlockNode.DEBUG(ExprNode.parse(parts[0].token.content))
		}
		val SET = com.soywiz.korte.Tag("set", setOf(), null) { parts ->
			val main = parts[0]
			val tr = ExprNode.Token.tokenize(main.token.content)
			val varname = ExprNode.parseId(tr)
			ExprNode.expect(tr, "=")
			val expr = ExprNode.parseExpr(tr)
			BlockNode.SET(varname, expr)
		}
	}
}
