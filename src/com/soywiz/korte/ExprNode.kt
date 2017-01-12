package com.soywiz.korte

import com.soywiz.korio.error.invalidOp
import com.soywiz.korio.util.Dynamic
import com.soywiz.korio.util.ListReader
import com.soywiz.korio.util.StrReader
import com.soywiz.korio.util.isLetterDigitOrUnderscore

interface ExprNode {
	fun eval(context: Template.Context): Any?

	data class VAR(val name: String) : com.soywiz.korte.ExprNode {
		override fun eval(context: Template.Context): Any? = context.scope[name]
	}

	data class LIT(val value: Any?) : com.soywiz.korte.ExprNode {
		override fun eval(context: Template.Context): Any? = value
	}

	data class ARRAY_LIT(val items: List<com.soywiz.korte.ExprNode>) : com.soywiz.korte.ExprNode {
		override fun eval(context: Template.Context): Any? = items.map { it.eval(context) }
	}

	data class FILTER(val name: String, val expr: com.soywiz.korte.ExprNode, val params: List<com.soywiz.korte.ExprNode>) : com.soywiz.korte.ExprNode {
		override fun eval(context: Template.Context): Any? {
			val filter = context.config.filters[name] ?: invalidOp("Unknown filter '$name'")
			return filter.eval(expr.eval(context), params.map { it.eval(context) })
		}
	}

	data class ACCESS(val expr: com.soywiz.korte.ExprNode, val name: com.soywiz.korte.ExprNode) : com.soywiz.korte.ExprNode {
		override fun eval(context: Template.Context): Any? {
			val obj = expr.eval(context)
			val key = name.eval(context)
			try {
				return Dynamic.accessAny(obj, key)
			} catch (t: Throwable) {
				try {
					return Dynamic.callAny(obj, key, listOf())
				} catch (t: Throwable) {
					return null
				}
			}
		}
	}

	data class CALL(val method: com.soywiz.korte.ExprNode, val args: List<com.soywiz.korte.ExprNode>) : com.soywiz.korte.ExprNode {
		override fun eval(context: Template.Context): Any? {
			if (method !is com.soywiz.korte.ExprNode.ACCESS) {
				return Dynamic.callAny(method.eval(context), args.map { it.eval(context) })
			} else {
				return Dynamic.callAny(method.expr.eval(context), method.name.eval(context), args.map { it.eval(context) })
			}
		}
	}

	data class BINOP(val l: com.soywiz.korte.ExprNode, val r: com.soywiz.korte.ExprNode, val op: String) : com.soywiz.korte.ExprNode {
		override fun eval(context: Template.Context): Any? = Dynamic.binop(l.eval(context), r.eval(context), op)
	}

	data class UNOP(val r: com.soywiz.korte.ExprNode, val op: String) : com.soywiz.korte.ExprNode {
		override fun eval(context: Template.Context): Any? = Dynamic.unop(r.eval(context), op)
	}

	companion object {
		fun ListReader<Token>.expectPeek(vararg types: String): com.soywiz.korte.ExprNode.Token {
			val token = this.peek()
			if (token.text !in types) throw java.lang.RuntimeException("Expected ${types.joinToString(", ")} but found '${token.text}'")
			return token
		}

		fun ListReader<Token>.expect(vararg types: String): com.soywiz.korte.ExprNode.Token {
			val token = this.read()
			if (token.text !in types) throw java.lang.RuntimeException("Expected ${types.joinToString(", ")}")
			return token
		}

		fun parse(str: String): com.soywiz.korte.ExprNode {
			val tokens = com.soywiz.korte.ExprNode.Token.Companion.tokenize(str)
			return com.soywiz.korte.ExprNode.Companion.parseFullExpr(tokens)
		}

		fun parseId(r: ListReader<Token>): String {
			return r.read().text
		}

		fun expect(r: ListReader<Token>, vararg tokens: String) {
			val token = r.read()
			if (token.text !in tokens) invalidOp("Expected ${tokens.joinToString(", ")} but found $token")
		}

		fun parseFullExpr(r: ListReader<Token>): com.soywiz.korte.ExprNode {
			val result = com.soywiz.korte.ExprNode.Companion.parseExpr(r)
			if (r.hasMore && r.peek() !is com.soywiz.korte.ExprNode.Token.TEnd) {
				invalidOp("Expected expression at " + r.peek() + " :: " + r.list.map { it.text }.joinToString(""))
			}
			return result
		}

		private val BINOPS = setOf(
			"+", "-", "*", "/", "%",
			"==", "!=", "<", ">", "<=", ">=", "<=>",
			"&&", "||"
		)

		fun parseExpr(r: ListReader<Token>): com.soywiz.korte.ExprNode {
			var result = com.soywiz.korte.ExprNode.Companion.parseFinal(r)
			while (r.hasMore) {
				if (r.peek() !is com.soywiz.korte.ExprNode.Token.TOperator || r.peek().text !in com.soywiz.korte.ExprNode.Companion.BINOPS) break
				val operator = r.read().text
				val right = com.soywiz.korte.ExprNode.Companion.parseFinal(r)
				result = BINOP(result, right, operator)
			}
			// @TODO: Fix order!
			return result
		}

		private fun parseFinal(r: ListReader<Token>): com.soywiz.korte.ExprNode {

			var construct: com.soywiz.korte.ExprNode = when (r.peek().text) {
				"!", "~", "-", "+" -> {
					val op = r.read().text
					UNOP(parseFinal(r), op)
				}
				"(" -> {
					r.read()
					val result = com.soywiz.korte.ExprNode.Companion.parseExpr(r)
					if (r.read().text != ")") throw RuntimeException("Expected ')'")
					result
				}
			// Array literal
				"[" -> {
					val items = arrayListOf<com.soywiz.korte.ExprNode>()
					r.read()
					loop@ while (r.hasMore && r.peek().text != "]") {
						items += com.soywiz.korte.ExprNode.Companion.parseExpr(r)
						when (r.peek().text) {
							"," -> r.read()
							"]" -> continue@loop
							else -> invalidOp("Expected , or ]")
						}
					}
					r.expect("]")
					ARRAY_LIT(items)
				}
				else -> {
					if (r.peek() is com.soywiz.korte.ExprNode.Token.TNumber) {
						LIT(r.read().text.toDouble())
					} else if (r.peek() is com.soywiz.korte.ExprNode.Token.TString) {
						LIT((r.read() as Token.TString).processedValue)
					} else {
						VAR(r.read().text)
					}
				}
			}

			loop@ while (r.hasMore) {
				when (r.peek().text) {
					"." -> {
						r.read()
						val id = r.read().text
						construct = ACCESS(construct, LIT(id))
						continue@loop
					}
					"[" -> {
						r.read()
						val expr = com.soywiz.korte.ExprNode.Companion.parseExpr(r)
						construct = ACCESS(construct, expr)
						val end = r.read()
						if (end.text != "]") throw RuntimeException("Expected ']' but found $end")
					}
					"|" -> {
						r.read()
						val name = r.read().text
						val args = arrayListOf<com.soywiz.korte.ExprNode>()
						if (r.peek().text == "(") {
							r.read()
							callargsloop@ while (r.hasMore && r.peek().text != ")") {
								args += com.soywiz.korte.ExprNode.Companion.parseExpr(r)
								when (r.expectPeek(",", ")").text) {
									"," -> r.read()
									")" -> break@callargsloop
								}
							}
							r.expect(")")
						}
						construct = FILTER(name, construct, args)
					}
					"(" -> {
						r.read()
						val args = arrayListOf<com.soywiz.korte.ExprNode>()
						callargsloop@ while (r.hasMore && r.peek().text != ")") {
							args += com.soywiz.korte.ExprNode.Companion.parseExpr(r)
							when (r.expectPeek(",", ")").text) {
								"," -> r.read()
								")" -> break@callargsloop
							}
						}
						r.expect(")")
						construct = CALL(construct, args)
					}
					else -> break@loop
				}
			}
			return construct
		}
	}

	interface Token {
		val text: String

		data class TId(override val text: String) : com.soywiz.korte.ExprNode.Token
		data class TNumber(override val text: String) : com.soywiz.korte.ExprNode.Token
		data class TString(override val text: String, val processedValue: String) : com.soywiz.korte.ExprNode.Token
		data class TOperator(override val text: String) : com.soywiz.korte.ExprNode.Token
		data class TEnd(override val text: String = "") : com.soywiz.korte.ExprNode.Token

		companion object {
			private val OPERATORS = setOf(
				"(", ")",
				"[", "]",
				"{", "}",
				"&&", "||",
				"&", "|", "^",
				"==", "!=", "<", ">", "<=", ">=", "<=>",
				"+", "-", "*", "/", "%", "**",
				"!", "~",
				".", ",", ";", ":",
				"="
			)

			fun tokenize(str: String): ListReader<Token> {
				val r = StrReader(str)
				val out = arrayListOf<com.soywiz.korte.ExprNode.Token>()
				fun emit(str: com.soywiz.korte.ExprNode.Token) {
					out += str
				}
				while (r.hasMore) {
					val start = r.pos
					r.skipSpaces()
					val id = r.readWhile(Char::isLetterDigitOrUnderscore)
					if (id.isNotEmpty()) {
						if (id[0].isDigit()) emit(com.soywiz.korte.ExprNode.Token.TNumber(id)) else emit(com.soywiz.korte.ExprNode.Token.TId(id))
					}
					r.skipSpaces()
					if (r.peek(3) in com.soywiz.korte.ExprNode.Token.Companion.OPERATORS) emit(com.soywiz.korte.ExprNode.Token.TOperator(r.read(3)))
					if (r.peek(2) in com.soywiz.korte.ExprNode.Token.Companion.OPERATORS) emit(com.soywiz.korte.ExprNode.Token.TOperator(r.read(2)))
					if (r.peek(1) in com.soywiz.korte.ExprNode.Token.Companion.OPERATORS) emit(com.soywiz.korte.ExprNode.Token.TOperator(r.read(1)))
					if (r.peek() == '\'' || r.peek() == '"') {
						val strStart = r.read()
						val strBody = r.readUntil(strStart) ?: ""
						val strEnd = r.read()
						emit(com.soywiz.korte.ExprNode.Token.TString(strStart + strBody + strEnd, strBody))
					}
					val end = r.pos
					if (end == start) invalidOp("Don't know how to handle '${r.peek()}'")
				}
				emit(com.soywiz.korte.ExprNode.Token.TEnd())
				return ListReader(out)
			}
		}
	}
}
