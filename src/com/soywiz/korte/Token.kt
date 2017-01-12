package com.soywiz.korte

interface Token {
	data class TLiteral(val content: String) : Token
	data class TExpr(val content: String) : Token
	data class TTag(val name: String, val content: String) : Token

	companion object {
		fun tokenize(str: String): List<Token> {
			val out = arrayListOf<Token>()
			var lastPos = 0

			fun emit(token: Token) {
				if (token is TLiteral && token.content.isEmpty()) return
				out += token
			}

			var pos = 0
			while (pos < str.length) {
				val c = str[pos++]
				if (c == '{') {
					if (pos >= str.length) break
					val c2 = str[pos++]
					if (c2 == '{' || c2 == '%') {
						val startPos = pos - 2
						val pos2 = if (c2 == '{') str.indexOf("}}", pos) else str.indexOf("%}", pos)
						if (pos2 < 0) break
						val content = str.substring(pos, pos2).trim()

						if (lastPos != startPos) {
							emit(TLiteral(str.substring(lastPos until startPos)))
						}

						if (c2 == '{') {
							//println("expr: '$content'")
							emit(TExpr(content))
						} else {
							val parts = content.split(' ', limit = 2)
							//println("tag: '$content'")
							emit(TTag(parts[0], parts.getOrElse(1) { "" }))
						}
						pos = pos2 + 2
						lastPos = pos
					}
				}
			}
			emit(TLiteral(str.substring(lastPos, str.length)))
			return out
		}
	}
}
