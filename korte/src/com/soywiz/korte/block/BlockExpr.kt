package com.soywiz.korte.block

import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template
import com.soywiz.korte.toDynamicString

data class BlockExpr(val expr: ExprNode) : Block {
	override suspend fun eval(context: Template.EvalContext) = context.write(expr.eval(context).toDynamicString())
}