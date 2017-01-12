package com.soywiz.korte.block

import com.soywiz.korte.BlockNode
import com.soywiz.korte.Template

data class BlockText(val content: String) : BlockNode {
	override fun eval(context: Template.Context) = run { context.write(content) }
}