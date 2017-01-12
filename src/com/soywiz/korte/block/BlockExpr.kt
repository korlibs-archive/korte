package com.soywiz.korte.block

import com.soywiz.korte.BlockNode
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template
import com.soywiz.korte.util.Dynamic

data class BlockExpr(val expr: ExprNode) : BlockNode {
	override fun eval(context: Template.Context) = run { context.write(Dynamic.toString(expr.eval(context))) }
}