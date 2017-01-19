package com.soywiz.korte.block

import com.soywiz.korte.Block
import com.soywiz.korte.Template

data class BlockBlock(val name: String) : Block {
	override suspend fun eval(context: Template.EvalContext) {
		context.rootTemplate.getBlock(context, name).eval(context)
	}
}
