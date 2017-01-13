package com.soywiz.korte

import com.soywiz.korio.async.asyncFun
import com.soywiz.korio.stream.openAsync
import com.soywiz.korio.util.Dynamic
import com.soywiz.korio.util.Extra
import com.soywiz.korio.vfs.MemoryVfs
import com.soywiz.korte.Token
import com.soywiz.korte.block.BlockCapture
import com.soywiz.korte.block.BlockExtends
import com.soywiz.korte.block.BlockGroup
import com.soywiz.korte.block.BlockText
import java.util.*
import kotlin.collections.set

class Template internal constructor(
	val templates: Templates,
	val template: String,
	val config: TemplateConfig = TemplateConfig()
) : Extra by Extra.Mixin() {
	// @TODO: Move to parse plugin + extra
	var frontMatter: Map<String, Any?>? = null

	val blocks = hashMapOf<String, Block>()
	val parseContext = ParseContext(this, config)
	val templateTokens = Token.Companion.tokenize(template)
	lateinit var rootNode: Block; private set

	suspend fun init(): Template = asyncFun {
		rootNode = Block.parse(templateTokens, parseContext)
		// @TODO: Move to parse plugin + extra
		if (frontMatter != null) {
			val layout = frontMatter?.get("layout")
			if (layout != null) {
				rootNode = BlockGroup(listOf(
					BlockCapture("content", rootNode),
					BlockExtends(ExprNode.LIT(layout))
				))
			}
		}
		this
	}

	class ParseContext(val template: Template, val config: TemplateConfig) {
		val templates: Templates get() = template.templates
	}

	class Scope(val map: Any?, val parent: Template.Scope? = null) : Dynamic.Context {
		// operator
		suspend fun get(key: Any?): Any? = asyncFun { map.dynamicGet(key) ?: parent?.get(key) }

		// operator
		suspend fun set(key: Any?, value: Any?): Unit = asyncFun<Unit> { map.dynamicSet(key, value) }
	}

	suspend fun eval(context: Template.EvalContext) = asyncFun {
		val oldParentTemplate = context.parentTemplate
		val oldCurrentTemplate = context.currentTemplate
		try {
			context.parentTemplate = context.templateStack.lastOrNull()
			context.currentTemplate = this@Template
			context.templateStack.addLast(this@Template)
			try {
				context.createScope { rootNode.eval(context) }
			} finally {
				context.templateStack.removeLast()
			}
		} catch (e: InterruptedException) {
		} finally {
			context.parentTemplate = oldParentTemplate
			context.currentTemplate = oldCurrentTemplate
		}
	}


	operator suspend fun invoke(args: Any?): String = asyncFun {
		val str = StringBuilder()
		val scope = Scope(args)
		if (frontMatter != null) for ((k, v) in frontMatter!!) scope.set(k, v)
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
		var write: (str: String) -> Unit,
		var templateStack: LinkedList<Template> = LinkedList()
	) {
		val templates = rootTemplate.templates

		var parentTemplate: Template? = null

		inline fun capture(callback: () -> Unit): String = this.run {
			var out = ""
			val old = write
			try {
				write = { out += it }
				callback()
			} finally {
				write = old
			}
			out
		}

		inline fun tempDropTemplate(callback: () -> Unit) = this.apply {
			val oldParentTemplate = parentTemplate
			val oldCurrentTemplate = currentTemplate
			val oldTemplateStack = this@EvalContext.templateStack.removeLast()
			try {
				currentTemplate = oldTemplateStack
				parentTemplate = templateStack.last
				callback()
			} finally {
				templateStack.addLast(oldTemplateStack)
				parentTemplate = oldParentTemplate
				currentTemplate = oldCurrentTemplate
			}
		}

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
