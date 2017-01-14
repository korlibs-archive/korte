package com.soywiz.korte.block

import com.soywiz.korio.async.asyncFun
import com.soywiz.korio.util.Dynamic
import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template
import com.soywiz.korte.toDynamicString

data class BlockExtends(val expr: ExprNode) : Block, Dynamic.Context {
	override suspend fun eval(context: Template.EvalContext) = asyncFun<Unit> {
		val result = expr.eval(context)
		val parentTemplate = context.rootTemplate.templates.getLayout(result.toDynamicString())
		parentTemplate.eval(context)
		throw InterruptedException()
		//context.template.parent
	}
}
