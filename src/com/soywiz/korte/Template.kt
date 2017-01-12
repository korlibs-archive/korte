package com.soywiz.korte

import com.soywiz.korio.stream.openAsync
import com.soywiz.korio.util.quote
import com.soywiz.korte.BlockNode
import com.soywiz.korte.Tag
import com.soywiz.korte.Token
import kotlin.collections.set

class Template(
	val context: com.soywiz.korte.TemplateFactoryContext,
	val template: String,
	val config: com.soywiz.korte.Template.Config = Config()
) {
	val templateTokens = Token.Companion.tokenize(template)
	val node = BlockNode.Companion.parse(templateTokens, config)

	class Config(
		extraTags: List<Tag> = listOf(),
		extraFilters: List<com.soywiz.korte.Template.Filter> = listOf()
	) {
		val integratedFilters = listOf(
			com.soywiz.korte.Template.Filter("length") { subject, _ -> com.soywiz.korio.util.Dynamic.length(subject) },
			com.soywiz.korte.Template.Filter("capitalize") { subject, _ -> com.soywiz.korio.util.Dynamic.toString(subject).toLowerCase().capitalize() },
			com.soywiz.korte.Template.Filter("upper") { subject, _ -> com.soywiz.korio.util.Dynamic.toString(subject).toUpperCase() },
			com.soywiz.korte.Template.Filter("lower") { subject, _ -> com.soywiz.korio.util.Dynamic.toString(subject).toLowerCase() },
			com.soywiz.korte.Template.Filter("trim") { subject, _ -> com.soywiz.korio.util.Dynamic.toString(subject).trim() },
			com.soywiz.korte.Template.Filter("quote") { subject, _ -> com.soywiz.korio.util.Dynamic.toString(subject).quote() },
			com.soywiz.korte.Template.Filter("join") { subject, args -> com.soywiz.korio.util.Dynamic.toIterable(subject).map { com.soywiz.korio.util.Dynamic.toString(it) }.joinToString(com.soywiz.korio.util.Dynamic.toString(args[0])) },
			com.soywiz.korte.Template.Filter("file_exists") { subject, _ -> java.io.File(com.soywiz.korio.util.Dynamic.toString(subject)).exists() }
		)

		private val allTags = listOf(Tag.Companion.EMPTY, Tag.Companion.IF, Tag.Companion.FOR, Tag.Companion.SET, Tag.Companion.DEBUG) + extraTags
		private val allFilters = integratedFilters + extraFilters

		val tags = hashMapOf<String, Tag>().apply {
			for (tag in allTags) {
				this[tag.name] = tag
				for (alias in tag.aliases) this[alias] = tag
			}
		}

		val filters = hashMapOf<String, com.soywiz.korte.Template.Filter>().apply {
			for (filter in allFilters) this[filter.name] = filter
		}
	}

	data class Filter(val name: String, val eval: (subject: Any?, args: List<Any?>) -> Any?)

	class Scope(val map: Any?, val parent: com.soywiz.korte.Template.Scope? = null) {
		operator fun get(key: Any?): Any? {
			return com.soywiz.korio.util.Dynamic.accessAny(map, key) ?: parent?.get(key)
		}

		operator fun set(key: Any?, value: Any?) {
			com.soywiz.korio.util.Dynamic.setAny(map, key, value)
		}
	}

	operator fun invoke(args: Any?): String {
		val str = StringBuilder()
		val context = com.soywiz.korte.Template.Context(Scope(args), config, write = { str.append(it) })
		context.createScope { node.eval(context) }
		return str.toString()
	}

	class Context(var scope: com.soywiz.korte.Template.Scope, val config: com.soywiz.korte.Template.Config, val write: (str: String) -> Unit) {
		inline fun createScope(callback: () -> Unit) = this.apply {
			val old = this.scope
			this.scope = com.soywiz.korte.Template.Scope(hashMapOf<Any?, Any?>(), old)
			callback()
			this.scope = old
		}
	}
}

suspend fun Template(template: String, config: com.soywiz.korte.Template.Config = com.soywiz.korte.Template.Config()): com.soywiz.korte.Template = com.soywiz.korio.async.asyncFun {
	com.soywiz.korte.TemplateFactoryContext(com.soywiz.korio.vfs.MemoryVfs(mapOf("template" to template.toByteArray().openAsync())), config).get("template")
}
