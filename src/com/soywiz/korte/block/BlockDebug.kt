package com.soywiz.korte.block

import com.soywiz.korte.BlockNode
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template

data class BlockDebug(val expr: ExprNode) : BlockNode {
	override fun eval(context: Template.Context) = run {
		println(expr.eval(context))
	}
}
