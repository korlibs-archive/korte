package com.soywiz.korte.tag

import com.soywiz.korte.ExprNode
import com.soywiz.korte.Tag
import com.soywiz.korte.block.BlockCapture

val CaptureTag = Tag("capture", setOf(), null) {
	val main = chunks[0]
	val tr = ExprNode.Token.tokenize(main.tag.content)
	val varname = ExprNode.parseId(tr)
	BlockCapture(varname, main.body)
}