package com.soywiz.korte.block

import com.soywiz.korio.async.asyncFun
import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template
import com.soywiz.korte.util.Dynamic

data class BlockIf(val cond: ExprNode, val trueContent: Block, val falseContent: Block?) : Block {
	override suspend fun eval(context: Template.EvalContext) = asyncFun<Unit> {
		if (Dynamic.toBool(cond.eval(context))) {
			trueContent.eval(context)
		} else {
			falseContent?.eval(context)
		}
	}
}