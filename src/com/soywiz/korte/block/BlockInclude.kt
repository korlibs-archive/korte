package com.soywiz.korte.block

import com.soywiz.korio.async.asyncFun
import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template
import com.soywiz.korte.util.toDynamicString

data class BlockInclude(val fileNameExpr: ExprNode) : Block {
	override suspend fun eval(context: Template.EvalContext) = asyncFun<Unit> {
		val fileName = fileNameExpr.eval(context).toDynamicString()
		context.templates.get(fileName).eval(context)
	}
}