package com.soywiz.korte.block

import com.soywiz.korio.util.Dynamic
import com.soywiz.korte.Block
import com.soywiz.korte.Template

data class BlockMacro(val funcname: String, val args: List<String>, val body: Block) : Block, Dynamic.Context {
	override suspend fun eval(context: Template.EvalContext) {
		context.macros[funcname] = Template.Macro(funcname, args, body)
	}
}