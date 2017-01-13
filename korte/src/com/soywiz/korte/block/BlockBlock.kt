package com.soywiz.korte.block

import com.soywiz.korio.async.asyncFun
import com.soywiz.korte.Block
import com.soywiz.korte.Template

data class BlockBlock(val name: String) : Block {
	override suspend fun eval(context: Template.EvalContext) = asyncFun<Unit> {
		context.rootTemplate.getBlock(context, name).eval(context)
	}
}
