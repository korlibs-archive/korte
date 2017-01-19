package com.soywiz.korte.block

import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template

data class BlockDebug(val expr: ExprNode) : Block {
	override suspend fun eval(context: Template.EvalContext) {
		println(expr.eval(context))
	}
}
