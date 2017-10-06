package com.soywiz.korte

import com.soywiz.korio.lang.toByteArray
import com.soywiz.korio.stream.openAsync
import com.soywiz.korio.util.Extra
import com.soywiz.korio.vfs.MemoryVfs
import kotlin.collections.set

class Template internal constructor(
	val name: String,
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

	suspend fun init(): Template {
		rootNode = Block.parse(templateTokens, parseContext)
		// @TODO: Move to parse plugin + extra
		if (frontMatter != null) {
			val layout = frontMatter?.get("layout")
			if (layout != null) {
				rootNode = DefaultBlocks.BlockGroup(listOf(
					DefaultBlocks.BlockCapture("content", rootNode),
					DefaultBlocks.BlockExtends(ExprNode.LIT(layout))
				))
			}
		}
		return this
	}

	class ParseContext(val template: Template, val config: TemplateConfig) {
		val templates: Templates get() = template.templates
	}

	class Scope(val map: Any?, val parent: Template.Scope? = null) {
		// operator
		suspend fun get(key: Any?): Any? = map.dynamicGet(key) ?: parent?.get(key)

		// operator
		suspend fun set(key: Any?, value: Any?): Unit {
			map.dynamicSet(key, value)
		}
	}

	data class ExecResult(val context: Template.EvalContext, val str: String)

	interface DynamicInvokable {
		suspend fun invoke(ctx: Template.EvalContext, args: List<Any?>): Any?
	}

	class Macro(val name: String, val argNames: List<String>, val code: Block) : DynamicInvokable {
		override suspend fun invoke(ctx: Template.EvalContext, args: List<Any?>): Any? {
			return ctx.createScope {
				for ((key, value) in this.argNames.zip(args)) {
					ctx.scope.set(key, value)
				}
				RawString(ctx.capture {
					code.eval(ctx)
				})
			}
		}
	}

	data class BlockInTemplateEval(val name: String, val block: Block, val template: TemplateEvalContext) {
		val parent: BlockInTemplateEval?
			get() {
				return template.parent?.getBlockOrNull(name)
			}

		suspend fun eval(ctx: EvalContext) = ctx.setTempTemplate(template) {
			val oldBlock = ctx.currentBlock
			try {
				ctx.currentBlock = this
				return@setTempTemplate block.eval(ctx)
			} finally {
				ctx.currentBlock = oldBlock
			}
		}
	}

	class TemplateEvalContext(val template: Template) {
		val name: String = template.name
		val templates: Templates get() = template.templates

		var parent: TemplateEvalContext? = null
		val root: TemplateEvalContext get() = parent?.root ?: this

		fun getBlockOrNull(name: String): BlockInTemplateEval? = template.blocks[name]?.let { BlockInTemplateEval(name, it, this@TemplateEvalContext) } ?: parent?.getBlockOrNull(name)
		fun getBlock(name: String): BlockInTemplateEval = getBlockOrNull(name) ?: BlockInTemplateEval(name, DefaultBlocks.BlockText(""), this)

		suspend fun exec(args: Any?): ExecResult {
			val str = StringBuilder()
			val scope = Scope(args)
			if (template.frontMatter != null) for ((k, v) in template.frontMatter!!) scope.set(k, v)
			val context = Template.EvalContext(this, scope, template.config, write = { str.append(it) })
			eval(context)
			return ExecResult(context, str.toString())
		}

		suspend fun exec(vararg args: Pair<String, Any?>): ExecResult = exec(hashMapOf(*args))

		operator suspend fun invoke(args: Any?): String = exec(args).str
		operator suspend fun invoke(vararg args: Pair<String, Any?>): String = exec(hashMapOf(*args)).str

		suspend fun eval(context: Template.EvalContext) {
			try {
				context.setTempTemplate(this) {
					context.createScope { template.rootNode.eval(context) }
				}
			} catch (e: StopEvaluatingException) {
			}
		}
	}

	class StopEvaluatingException : Exception()

	class EvalContext(
		var currentTemplate: TemplateEvalContext,
		var scope: Template.Scope,
		val config: TemplateConfig,
		var write: (str: String) -> Unit
	) {
		val leafTemplate: TemplateEvalContext = currentTemplate
		val templates = currentTemplate.templates
		val macros = hashMapOf<String, Macro>()
		var currentBlock: BlockInTemplateEval? = null

		inline fun <T> setTempTemplate(template: TemplateEvalContext, callback: () -> T): T {
			val oldTemplate = this.currentTemplate
			try {
				this.currentTemplate = template
				return callback()
			} finally {
				this.currentTemplate = oldTemplate
			}
		}

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

		inline fun captureRaw(callback: () -> Unit): RawString = RawString(capture(callback))

		inline fun <T> createScope(callback: () -> T): T {
			val old = this.scope
			try {
				this.scope = Template.Scope(hashMapOf<Any?, Any?>(), old)
				return callback()
			} finally {
				this.scope = old
			}
		}
	}

	fun addBlock(name: String, body: Block) {
		blocks[name] = body
	}

	suspend operator fun invoke(hashMap: Any?): String = Template.TemplateEvalContext(this).invoke(hashMap)
	suspend operator fun invoke(vararg args: Pair<String, Any?>): String = Template.TemplateEvalContext(this).invoke(*args)
}

suspend fun Template(template: String, config: TemplateConfig = TemplateConfig()): Template {
	return Templates(MemoryVfs(mapOf("template" to template.toByteArray(config.charset).openAsync())), config = config).get("template")
}
