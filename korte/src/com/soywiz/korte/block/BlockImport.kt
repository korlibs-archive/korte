package com.soywiz.korte.block

import com.soywiz.korio.util.Dynamic
import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template

data class BlockImport(val fileExpr: ExprNode, val exportName: String) : Block, Dynamic.Context {
	override suspend fun eval(context: Template.EvalContext) {
		val ctx = context.templates.getInclude(fileExpr.eval(context).toString()).exec().context
		context.scope.set(exportName, ctx.macros)
	}
}