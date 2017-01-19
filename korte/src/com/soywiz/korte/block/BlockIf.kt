package com.soywiz.korte.block

import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template
import com.soywiz.korte.toDynamicBool

data class BlockIf(val cond: ExprNode, val trueContent: Block, val falseContent: Block?) : Block {
	override suspend fun eval(context: Template.EvalContext) {
		if (cond.eval(context).toDynamicBool()) {
			trueContent.eval(context)
		} else {
			falseContent?.eval(context)
		}
	}
}