package com.soywiz.korte.block

import com.soywiz.korte.Block
import com.soywiz.korte.RawString
import com.soywiz.korte.Template

data class BlockCapture(val varname: String, val content: Block) : Block {
	override suspend fun eval(context: Template.EvalContext) {
		val result = context.capture {
			content.eval(context)
		}
		context.scope.set(varname, RawString(result))
	}
}
