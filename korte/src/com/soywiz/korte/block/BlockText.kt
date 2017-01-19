package com.soywiz.korte.block

import com.soywiz.korte.Block
import com.soywiz.korte.Template

data class BlockText(val content: String) : Block {
	override suspend fun eval(context: Template.EvalContext) {
		context.write(content)
	}
}