package com.soywiz.korte

data class Tag(val name: String, val nextList: Set<String>, val end: String?, val aliases: List<String> = listOf(), val buildNode: (context: Template.ParseContext, parts: List<Tag.Part>) -> Block) {
	data class Part(val token: Token.TTag, val body: Block)
}
