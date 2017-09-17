package com.soywiz.korte

import com.soywiz.korio.util.Dynamic

object DefaultBlocks {
	data class BlockBlock(val name: String) : Block {
		override suspend fun eval(context: Template.EvalContext) {
			val oldBlockName = context.currentBlockName
			try {
				context.currentBlockName = name
				context.rootTemplate.getBlock(context, name).eval(context)
			} finally {
				context.currentBlockName = oldBlockName
			}
		}
	}

	data class BlockCapture(val varname: String, val content: Block) : Block {
		override suspend fun eval(context: Template.EvalContext) {
			val result = context.capture {
				content.eval(context)
			}
			context.scope.set(varname, RawString(result))
		}
	}

	data class BlockDebug(val expr: ExprNode) : Block {
		override suspend fun eval(context: Template.EvalContext) {
			println(expr.eval(context))
		}
	}

	data class BlockExpr(val expr: ExprNode) : Block {
		override suspend fun eval(context: Template.EvalContext) {
			context.write(expr.eval(context).toEscapedString())
		}
	}

	data class BlockExtends(val expr: ExprNode) : Block, Dynamic.Context {
		override suspend fun eval(context: Template.EvalContext) {
			val result = expr.eval(context)
			val parentTemplate = context.rootTemplate.templates.getLayout(result.toDynamicString())
			parentTemplate.eval(context)
			throw InterruptedException()
			//context.template.parent
		}
	}

	data class BlockFor(val varnames: List<String>, val expr: ExprNode, val loop: Block, val elseNode: Block?) : Block {
		override suspend fun eval(context: Template.EvalContext) {
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

	data class BlockGroup(val children: List<Block>) : Block {
		override suspend fun eval(context: Template.EvalContext) {
			for (n in children) n.eval(context)
		}
	}

	data class BlockIf(val cond: ExprNode, val trueContent: Block, val falseContent: Block?) : Block {
		override suspend fun eval(context: Template.EvalContext) {
			if (cond.eval(context).toDynamicBool()) {
				trueContent.eval(context)
			} else {
				falseContent?.eval(context)
			}
		}
	}

	data class BlockImport(val fileExpr: ExprNode, val exportName: String) : Block, Dynamic.Context {
		override suspend fun eval(context: Template.EvalContext) {
			val ctx = context.templates.getInclude(fileExpr.eval(context).toString()).exec().context
			context.scope.set(exportName, ctx.macros)
		}
	}

	data class BlockInclude(val fileNameExpr: ExprNode) : Block, Dynamic.Context {
		override suspend fun eval(context: Template.EvalContext) {
			val fileName = fileNameExpr.eval(context).toDynamicString()
			context.templates.getInclude(fileName).eval(context)
		}
	}

	data class BlockMacro(val funcname: String, val args: List<String>, val body: Block) : Block, Dynamic.Context {
		override suspend fun eval(context: Template.EvalContext) {
			context.macros[funcname] = Template.Macro(funcname, args, body)
		}
	}

	data class BlockSet(val varname: String, val expr: ExprNode) : Block {
		override suspend fun eval(context: Template.EvalContext) {
			context.scope.set(varname, expr.eval(context))
		}
	}

	data class BlockText(val content: String) : Block {
		override suspend fun eval(context: Template.EvalContext) {
			context.write(content)
		}
	}
}
