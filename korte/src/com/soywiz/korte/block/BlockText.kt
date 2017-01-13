package com.soywiz.korte.block

import com.soywiz.korio.async.asyncFun
import com.soywiz.korte.Block
import com.soywiz.korte.Template

data class BlockText(val content: String) : Block {
	override suspend fun eval(context: Template.EvalContext) = asyncFun<Unit> { context.write(content) }
}