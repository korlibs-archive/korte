package com.soywiz.korte.block

import com.soywiz.korte.BlockNode
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template

data class BlockSet(val varname: String, val expr: ExprNode) : BlockNode {
	override fun eval(context: Template.Context) = run {
		context.scope[varname] = expr.eval(context)
	}
}
