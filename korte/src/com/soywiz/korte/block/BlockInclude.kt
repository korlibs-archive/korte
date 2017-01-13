package com.soywiz.korte.block

import com.soywiz.korio.async.asyncFun
import com.soywiz.korio.util.Dynamic
import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template

data class BlockInclude(val fileNameExpr: ExprNode) : Block, Dynamic.Context {
	override suspend fun eval(context: Template.EvalContext) = asyncFun<Unit> {
		val fileName = fileNameExpr.eval(context).toDynamicString()
		context.templates.get(fileName).eval(context)
	}
}