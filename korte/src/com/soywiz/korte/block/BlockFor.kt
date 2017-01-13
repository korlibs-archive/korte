package com.soywiz.korte.block

import com.soywiz.korio.async.asyncFun
import com.soywiz.korio.util.Dynamic
import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Template

data class BlockFor(val varnames: List<String>, val expr: ExprNode, val loop: Block, val elseNode: Block?) : Block {
	override suspend fun eval(context: Template.EvalContext) = asyncFun<Unit> {
		context.createScope {
			var index = 0
			val items = expr.eval(context).toDynamicList()
			val loopValue = hashMapOf<String, Any?>()
			context.scope.set("loop", loopValue)
			loopValue["length"] = items.size
			for (v in items) {
				if (v is Pair<*, *> && varnames.size >= 2) {
					context.scope.set(varnames[0], v.first)
					context.scope.set(varnames[1], v.second)
				} else {
					context.scope.set(varnames[0], v)
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
