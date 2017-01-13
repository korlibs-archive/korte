package com.soywiz.korte

import com.soywiz.korio.async.asyncFun
import com.soywiz.korio.stream.openAsync
import com.soywiz.korio.vfs.MemoryVfs
import com.soywiz.korte.Token
import com.soywiz.korte.block.BlockText
import com.soywiz.korte.util.Dynamic
import java.util.*
import kotlin.collections.set

class Template internal constructor(
	val templates: Templates,
	val template: String,
	val config: TemplateConfig = TemplateConfig()
) {
	val frontMatter = hashMapOf<String, Any?>()
	val blocks = hashMapOf<String, Block>()
	val parseContext = ParseContext(this, config)
	val templateTokens = Token.Companion.tokenize(template)
	lateinit var rootNode: Block

	suspend fun init(): Template = asyncFun {
		rootNode = Block.parse(templateTokens, parseContext)
		this
	}

	class ParseContext(val template: Template, val config: TemplateConfig) {
		val templates: Templates get() = template.templates
	}


	class Scope(val map: Any?, val parent: Template.Scope? = null) {
		suspend fun get(key: Any?): Any? = asyncFun { Dynamic.accessAny(map, key) ?: parent?.get(key) }
		suspend fun set(key: Any?, value: Any?): Unit = run { Dynamic.setAny(map, key, value) }
	}

	suspend fun eval(context: Template.EvalContext) = asyncFun {
		val prevTemplate = context.currentTemplate
		try {
			context.currentTemplate = this@Template
			context.templateStack.addLast(this@Template)
			try {
				context.createScope { rootNode.eval(context) }
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
		val scope = Scope(args)
		for ((k, v) in frontMatter) scope.set(k, v)
		val context = Template.EvalContext(this, this, scope, config, write = { str.append(it) })
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
		val config: TemplateConfig,
		val write: (str: String) -> Unit,
		var templateStack: LinkedList<Template> = LinkedList()
	) {
		val templates = rootTemplate.templates

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

suspend fun Template(template: String, config: TemplateConfig = TemplateConfig()): Template = asyncFun {
	Templates(MemoryVfs(mapOf("template" to template.toByteArray().openAsync())), config).get("template")
}
