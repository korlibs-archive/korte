package com.soywiz.korte.tag

import com.soywiz.korio.util.Dynamic
import com.soywiz.korte.*
import com.soywiz.korte.block.*
import com.soywiz.korte.filter.DefaultFilters

@Suppress("unused")
object DefaultTags {
	@JvmStatic
	val BlockTag = Tag("block", setOf(), setOf("end", "endblock")) {
		val part = chunks.first()
		val tokens = ExprNode.Token.tokenize(part.tag.content)
		val name = ExprNode.parseId(tokens)
		if (name.isEmpty()) throw IllegalArgumentException("block without name")
		context.template.addBlock(name, part.body)
		BlockBlock(name)
	}

	@JvmStatic
	val Capture = Tag("capture", setOf(), null) {
		val main = chunks[0]
		val tr = ExprNode.Token.tokenize(main.tag.content)
		val varname = ExprNode.parseId(tr)
		BlockCapture(varname, main.body)
	}

	@JvmStatic
	val Debug = Tag("debug", setOf(), null) {
		BlockDebug(ExprNode.parse(chunks[0].tag.content))
	}

	@JvmStatic
	val Empty = Tag("", setOf(""), null) {
		Block.group(chunks.map { it.body })
	}

	@JvmStatic
	val Extends = Tag("extends", setOf(), null) {
		val part = chunks.first()
		val parent = ExprNode.parseExpr(ExprNode.Token.tokenize(part.tag.content))
		BlockExtends(parent)
	}

	@JvmStatic
	val For = Tag("for", setOf("else"), setOf("end", "endfor")) {
		val main = chunks[0]
		val elseTag = chunks.getOrNull(1)?.body
		val tr = ExprNode.Token.tokenize(main.tag.content)
		val varnames = arrayListOf<String>()
		do {
			varnames += ExprNode.parseId(tr)
		} while (tr.tryRead(",") != null)
		ExprNode.expect(tr, "in")
		val expr = ExprNode.parseExpr(tr)
		BlockFor(varnames, expr, main.body, elseTag)
	}

	@JvmStatic
	val If = Tag("if", setOf("else", "elseif"), setOf("end", "endif")) {
		val ifBranches = arrayListOf<Pair<ExprNode, Block>>()
		var elseBranch: Block? = null

		for (part in chunks) {
			when (part.tag.name) {
				"if", "elseif" -> {
					ifBranches += ExprNode.parse(part.tag.content) to part.body
				}
				"else" -> {
					elseBranch = part.body
				}
			}
		}
		val ifBranchesRev = ifBranches.reversed()
		var node: Block = BlockIf(ifBranchesRev.first().first, ifBranchesRev.first().second, elseBranch)
		for (branch in ifBranchesRev.takeLast(ifBranchesRev.size - 1)) {
			node = BlockIf(branch.first, branch.second, node)
		}

		node
	}

	@JvmStatic
	val Import = Tag("import", setOf(), null) {
		val part = chunks.first()
		val s = ExprNode.Token.tokenize(part.tag.content)
		val file = s.parseExpr()
		s.expect("as")
		val name = s.read().text
		BlockImport(file, name)
	}

	@JvmStatic
	val Include = Tag("include", setOf(), null) {
		val part = chunks.first()
		val fileName = ExprNode.parseExpr(part.tag.tokens)
		BlockInclude(fileName)
	}

	@JvmStatic
	val Macro = Tag("macro", setOf(), setOf("end", "endmacro")) {
		val part = chunks[0]
		val s = ExprNode.Token.tokenize(part.tag.content)
		val funcname = s.parseId()
		s.expect("(")
		val params = s.parseIdList()
		s.expect(")")
		BlockMacro(funcname, params, part.body)
	}

	@JvmStatic
	val Set = Tag("set", setOf(), null) {
		val main = chunks[0]
		val tr = ExprNode.Token.tokenize(main.tag.content)
		val varname = ExprNode.parseId(tr)
		ExprNode.expect(tr, "=")
		val expr = ExprNode.parseExpr(tr)
		BlockSet(varname, expr)
	}

	val ALL by lazy { Dynamic.getStaticTypedFields<Tag>(javaClass) }
}