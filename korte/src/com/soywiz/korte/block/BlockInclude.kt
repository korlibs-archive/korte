package com.soywiz.korte.block

import com.soywiz.korio.util.Dynamic
import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template
import com.soywiz.korte.toDynamicString

data class BlockInclude(val fileNameExpr: ExprNode) : Block, Dynamic.Context {
	override suspend fun eval(context: Template.EvalContext) {
		val fileName = fileNameExpr.eval(context).toDynamicString()
		context.templates.getInclude(fileName).eval(context)
	}
}