package com.soywiz.korte.block

import com.soywiz.korte.BlockNode
import com.soywiz.korte.Template

data class BlockGroup(val children: List<BlockNode>) : BlockNode {
	override fun eval(context: Template.Context) = run { for (n in children) n.eval(context) }
}
