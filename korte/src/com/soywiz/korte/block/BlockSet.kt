package com.soywiz.korte.block

import com.soywiz.korio.async.asyncFun
import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template

data class BlockSet(val varname: String, val expr: ExprNode) : Block {
	override suspend fun eval(context: Template.EvalContext) = asyncFun {
		context.scope.set(varname, expr.eval(context))
	}
}
