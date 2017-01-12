package com.soywiz.korte.block

import com.soywiz.korio.async.asyncFun
import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template
import com.soywiz.korte.util.Dynamic

data class BlockExpr(val expr: ExprNode) : Block {
	override suspend fun eval(context: Template.EvalContext) = asyncFun<Unit> {context.write(Dynamic.toString(expr.eval(context))) }
}