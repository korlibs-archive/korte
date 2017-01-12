package com.soywiz.korte.tag

import com.soywiz.korte.BlockNode
import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag

val IfTag = Tag("if", setOf("else", "elseif"), "end") { parts ->
	val ifBranches = arrayListOf<Pair<ExprNode, BlockNode>>()
	var elseBranch: BlockNode? = null

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
	var node: BlockNode = BlockNode.IF(ifBranchesRev.first().first, ifBranchesRev.first().second, elseBranch)
	for (branch in ifBranchesRev.takeLast(ifBranchesRev.size - 1)) {
		node = BlockNode.IF(branch.first, branch.second, node)
	}

	node
}