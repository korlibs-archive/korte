package com.soywiz.korte.tag

import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag
import com.soywiz.korte.block.BlockImport
import com.soywiz.korte.expect
import com.soywiz.korte.parseExpr

val TagImport = Tag("import", setOf(), null) {
	val part = chunks.first()
	val s = ExprNode.Token.tokenize(part.tag.content)
	val file = s.parseExpr()
	s.expect("as")
	val name = s.read().text
	BlockImport(file, name)
}