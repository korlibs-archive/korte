package com.soywiz.korte.block

import com.soywiz.korio.async.asyncFun
import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template

data class BlockCapture(val varname: String, val content: Block) : Block {
	override suspend fun eval(context: Template.EvalContext) = asyncFun<Unit> {
		val result = context.capture {
			content.eval(context)
		}
		context.scope.set(varname, result)
	}
}
