package com.soywiz.korte.block

import com.soywiz.korte.BlockNode
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template
import com.soywiz.korte.util.Dynamic

data class BlockIf(val cond: ExprNode, val trueContent: BlockNode, val falseContent: BlockNode?) : BlockNode {
	override fun eval(context: Template.Context) {
		if (Dynamic.toBool(cond.eval(context))) {
			trueContent.eval(context)
		} else {
			falseContent?.eval(context)
		}
	}
}