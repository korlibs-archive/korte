package com.soywiz.korte

data class Tag(val name: String, val nextList: Set<String>, val end: String?, val aliases: List<String> = listOf(), val buildNode: (parts: List<Tag.Part>) -> BlockNode) {
	data class Part(val token: Token.TTag, val body: BlockNode)
}
