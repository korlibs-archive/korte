package com.soywiz.korte

import com.soywiz.korio.error.invalidOp
import com.soywiz.korio.util.Dynamic
import com.soywiz.korio.util.ListReader

interface BlockNode {
	fun eval(context: Template.Context): Unit

	data class GROUP(val children: List<BlockNode>) : BlockNode {
		override fun eval(context: Template.Context) = run { for (n in children) n.eval(context) }
	}

	data class TEXT(val content: String) : BlockNode {
		override fun eval(context: Template.Context) = run { context.write(content) }
	}

	data class EXPR(val expr: ExprNode) : BlockNode {
		override fun eval(context: Template.Context) = run { context.write(Dynamic.toString(expr.eval(context))) }
	}

	data class IF(val cond: ExprNode, val trueContent: BlockNode, val falseContent: BlockNode?) : BlockNode {
		override fun eval(context: Template.Context) {
			if (Dynamic.toBool(cond.eval(context))) {
				trueContent.eval(context)
			} else {
				falseContent?.eval(context)
			}
		}
	}

	data class FOR(val varname: String, val expr: ExprNode, val loop: BlockNode) : BlockNode {
		override fun eval(context: Template.Context) {
			context.createScope {
				for (v in Dynamic.toIterable(expr.eval(context))) {
					context.scope[varname] = v
					loop.eval(context)
				}
			}
		}
	}

	data class SET(val varname: String, val expr: ExprNode) : BlockNode {
		override fun eval(context: Template.Context) = run {
			context.scope[varname] = expr.eval(context)
		}
	}

	data class DEBUG(val expr: ExprNode) : BlockNode {
		override fun eval(context: Template.Context) = run {
			println(expr.eval(context))
		}
	}

	companion object {
		fun group(children: List<BlockNode>): BlockNode = if (children.size == 1) children[0] else GROUP(children)

		fun parse(tokens: List<Token>, config: Template.Config): BlockNode {
			val tr = ListReader(tokens)
			fun handle(tag: Tag, token: Token.TTag): BlockNode {
				val parts = arrayListOf<Tag.Part>()
				var currentToken = token
				val children = arrayListOf<BlockNode>()

				fun emitPart() {
					parts += Tag.Part(currentToken, group(children))
				}

				loop@ while (!tr.eof) {
					val it = tr.read()
					when (it) {
						is Token.TLiteral -> children += TEXT(it.content)
						is Token.TExpr -> children += EXPR(ExprNode.parse(it.content))
						is Token.TTag -> {
							when (it.name) {
								tag.end -> break@loop
								in tag.nextList -> {
									emitPart()
									currentToken = it
									children.clear()
								}
								else -> {
									val newtag = config.tags[it.name] ?: invalidOp("Can't find tag ${it.name}")
									if (newtag.end != null) {
										children += handle(newtag, it)
									} else {
										children += newtag.buildNode(listOf(Tag.Part(it, TEXT(""))))
									}
								}
							}
						}
						else -> break@loop
					}
				}

				emitPart()

				return tag.buildNode(parts)
			}
			return handle(Tag.EMPTY, Token.TTag("", ""))
		}
	}
}
