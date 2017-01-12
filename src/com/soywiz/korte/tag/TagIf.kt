package com.soywiz.korte.tag

import com.soywiz.korte.Block
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag
import com.soywiz.korte.block.BlockIf

val TagIf = Tag("if", setOf("else", "elseif"), "end") { context, parts ->
	val ifBranches = arrayListOf<Pair<ExprNode, Block>>()
	var elseBranch: Block? = null

	for (part in parts) {
		when (part.token.name) {
			"if", "elseif" -> {
				ifBranches += ExprNode.parse(part.token.content) to part.body
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