package com.soywiz.korte.tag

import com.soywiz.korte.*
import com.soywiz.korte.block.BlockMacro

val TagMacro = Tag("macro", setOf(), setOf("end", "endmacro")) {
	val part = chunks[0]
	val s = ExprNode.Token.tokenize(part.tag.content)
	val funcname = s.parseId()
	s.expect("(")
	val params = s.parseIdList()
	s.expect(")")
	BlockMacro(funcname, params, part.body)
}