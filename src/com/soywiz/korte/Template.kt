package com.soywiz.korte

import com.soywiz.korio.async.asyncFun
import com.soywiz.korio.stream.openAsync
import com.soywiz.korio.vfs.MemoryVfs
import com.soywiz.korte.Block
import com.soywiz.korte.Token
import com.soywiz.korte.block.BlockText
import com.soywiz.korte.filter.*
import com.soywiz.korte.tag.*
import com.soywiz.korte.util.Dynamic
import java.util.*
import kotlin.collections.set

class Template(
	val factory: TemplateFactory,
	val template: String,
	val config: Template.Config = Config()
) {
	val blocks = hashMapOf<String, Block>()
	val parseContext = ParseContext(this, config)
	val templateTokens = Token.Companion.tokenize(template)
	val node = Block.Companion.parse(templateTokens, parseContext)

	class ParseContext(val template: Template, val config: Config)

	class Config(
		extraTags: List<Tag> = listOf(),
		extraFilters: List<Filter> = listOf()
	) {
		val integratedFilters = listOf(
			FilterLength, FilterCapitalize, FilterUpper,
			FilterLower, FilterTrim, FilterQuote, FilterJoin,
			FilterSlice, FilterReverse
		)

		val integratedTags = listOf(
			TagEmpty, TagIf, TagFor, SetTag, TagDebug, TagBlock, TagExtends
		)

		private val allTags = integratedTags + extraTags
		private val allFilters = integratedFilters + extraFilters

		val tags = hashMapOf<String, Tag>().apply {
			for (tag in allTags) {
				this[tag.name] = tag
				for (alias in tag.aliases) this[alias] = tag
			}
		}

		val filters = hashMapOf<String, Filter>().apply {
			for (filter in allFilters) this[filter.name] = filter
		}
	}

	class Scope(val map: Any?, val parent: Template.Scope? = null) {
		operator fun get(key: Any?): Any? = Dynamic.accessAny(map, key) ?: parent?.get(key)
		operator fun set(key: Any?, value: Any?): Unit = run { Dynamic.setAny(map, key, value) }
	}

	suspend fun eval(context: Template.EvalContext) = asyncFun {
		val prevTemplate = context.currentTemplate
		try {
			context.currentTemplate = this@Template
			context.templateStack.addLast(this@Template)
			try {
				context.createScope { node.eval(context) }
			} finally {
				context.templateStack.removeLast()
			}
		} catch (e: InterruptedException) {
		} finally {
			context.currentTemplate = prevTemplate
		}
	}


	operator suspend fun invoke(args: Any?): String = asyncFun {
		val str = StringBuilder()
		val context = Template.EvalContext(this, this, Scope(args), config, write = { str.append(it) })
		eval(context)
		str.toString()
	}

	operator suspend fun invoke(vararg args: Pair<String, Any?>): String {
		return invoke(hashMapOf(*args))
	}

	class EvalContext(
		val rootTemplate: Template,
		var currentTemplate: Template,
		var scope: Template.Scope,
		val config: Template.Config,
		val write: (str: String) -> Unit,
		var templateStack: LinkedList<Template> = LinkedList()
	) {
		inline fun createScope(callback: () -> Unit) = this.apply {
			val old = this.scope
			try {
				this.scope = Template.Scope(hashMapOf<Any?, Any?>(), old)
				callback()
			} finally {
				this.scope = old
			}
		}
	}

	fun getBlock(context: Template.EvalContext, name: String): Block {
		for (t in context.templateStack) {
			val block = t.blocks[name]
			if (block != null) return block
		}
		return BlockText("")
	}

	fun addBlock(name: String, body: Block) {
		blocks[name] = body
	}
}

suspend fun Template(template: String, config: Template.Config = Template.Config()): Template = asyncFun {
	TemplateFactory(MemoryVfs(mapOf("template" to template.toByteArray().openAsync())), config).get("template")
}
