package com.soywiz.korte.block

import com.soywiz.korio.async.asyncFun
import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template
import com.soywiz.korte.util.toDynamicString

data class BlockExtends(val expr: ExprNode) : Block {
	override suspend fun eval(context: Template.EvalContext) = asyncFun<Unit> {
		val result = expr.eval(context)
		val parentTemplate = context.rootTemplate.factory.get(result.toDynamicString())
		parentTemplate.eval(context)
		throw InterruptedException()
		//context.template.parent
	}
}
