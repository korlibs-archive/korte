package com.soywiz.korte.block

import com.soywiz.korte.BlockNode
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template
import com.soywiz.korte.util.Dynamic

data class BlockFor(val varnames: List<String>, val expr: ExprNode, val loop: BlockNode, val elseNode: BlockNode?) : BlockNode {
	override fun eval(context: Template.Context) {
		context.createScope {
			var index = 0
			val items = Dynamic.toIterable(expr.eval(context)).toList()
			val loopValue = hashMapOf<String, Any?>()
			context.scope["loop"] = loopValue
			loopValue["length"] = items.size
			for (v in items) {
				if (v is Pair<*, *> && varnames.size >= 2) {
					context.scope[varnames[0]] = v.first
					context.scope[varnames[1]] = v.second
				} else {
					context.scope[varnames[0]] = v
				}
				loopValue["index"] = index + 1
				loopValue["index0"] = index
				loopValue["revindex"] = items.size - index - 1
				loopValue["revindex0"] = items.size - index
				loopValue["first"] = (index == 0)
				loopValue["last"] = (index == items.size - 1)
				loop.eval(context)
				index++
			}
			if (index == 0) {
				elseNode?.eval(context)
			}
		}
	}
}
