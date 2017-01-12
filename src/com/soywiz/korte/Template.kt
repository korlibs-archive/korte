package com.soywiz.korte

import com.soywiz.korio.async.asyncFun
import com.soywiz.korio.stream.openAsync
import com.soywiz.korio.util.quote
import com.soywiz.korio.vfs.MemoryVfs
import com.soywiz.korte.BlockNode
import com.soywiz.korte.Token
import com.soywiz.korte.tag.*
import com.soywiz.korte.util.Dynamic
import kotlin.collections.set

class Template(
	val context: TemplateFactoryContext,
	val template: String,
	val config: Template.Config = Config()
) {
	val templateTokens = Token.Companion.tokenize(template)
	val node = BlockNode.Companion.parse(templateTokens, config)

	class Config(
		extraTags: List<Tag> = listOf(),
		extraFilters: List<Template.Filter> = listOf()
	) {
		val integratedFilters = listOf(
			Filter("length") { subject, _ -> Dynamic.length(subject) },
			Filter("capitalize") { subject, _ -> Dynamic.toString(subject).toLowerCase().capitalize() },
			Filter("upper") { subject, _ -> Dynamic.toString(subject).toUpperCase() },
			Filter("lower") { subject, _ -> Dynamic.toString(subject).toLowerCase() },
			Filter("trim") { subject, _ -> Dynamic.toString(subject).trim() },
			Filter("quote") { subject, _ -> Dynamic.toString(subject).quote() },
			Filter("join") { subject, args -> Dynamic.toIterable(subject).map { Dynamic.toString(it) }.joinToString(Dynamic.toString(args[0])) },
			Filter("file_exists") { subject, _ -> java.io.File(Dynamic.toString(subject)).exists() }
		)

		private val allTags = listOf(TagEmpty, TagIf, TagFor, SetTag, TagDebug) + extraTags
		private val allFilters = integratedFilters + extraFilters

		val tags = hashMapOf<String, Tag>().apply {
			for (tag in allTags) {
				this[tag.name] = tag
				for (alias in tag.aliases) this[alias] = tag
			}
		}

		val filters = hashMapOf<String, Template.Filter>().apply {
			for (filter in allFilters) this[filter.name] = filter
		}
	}

	data class Filter(val name: String, val eval: (subject: Any?, args: List<Any?>) -> Any?)

	class Scope(val map: Any?, val parent: Template.Scope? = null) {
		operator fun get(key: Any?): Any? = Dynamic.accessAny(map, key) ?: parent?.get(key)
		operator fun set(key: Any?, value: Any?): Unit = run { Dynamic.setAny(map, key, value) }
	}

	operator fun invoke(args: Any?): String {
		val str = StringBuilder()
		val context = Template.Context(Scope(args), config, write = { str.append(it) })
		context.createScope { node.eval(context) }
		return str.toString()
	}

	operator fun invoke(vararg args: Pair<String, Any?>): String {
		return invoke(hashMapOf(*args))
	}

	class Context(var scope: Template.Scope, val config: Template.Config, val write: (str: String) -> Unit) {
		inline fun createScope(callback: () -> Unit) = this.apply {
			val old = this.scope
			this.scope = Template.Scope(hashMapOf<Any?, Any?>(), old)
			callback()
			this.scope = old
		}
	}
}

suspend fun Template(template: String, config: Template.Config = Template.Config()): Template = asyncFun {
	TemplateFactoryContext(MemoryVfs(mapOf("template" to template.toByteArray().openAsync())), config).get("template")
}
