package com.soywiz.korte

import com.soywiz.korio.util.Dynamic
import com.soywiz.korio.util.umod

@Suppress("unused")
object DefaultFunctions {
	@JvmStatic
	val Cycle = TeFunction("cycle") { args, _ ->
		val list = args.getOrNull(0).toDynamicList()
		val index = args.getOrNull(1).toDynamicInt()
		list[index umod list.size]
	}

	@JvmStatic
	val Range = TeFunction("range") { args, _ ->
		val left = args.getOrNull(0)
		val right = args.getOrNull(1)
		val step = (args.getOrNull(2) ?: 1).toDynamicInt()
		if (left is Number || right is Number) {
			val l = left.toDynamicInt()
			val r = right.toDynamicInt()
			((l..r) step step).toList()
		} else {
			TODO("Unsupported '$left'/'$right' for ranges")
		}
	}

	@JvmStatic
	val Parent = TeFunction("parent") { _, ctx ->
		if (ctx.templateStack.isEmpty()) {
			""
		} else {
			//ctx.tempDropTemplate {
			ctx.tempDropFirstTemplate {
				val current = ctx.currentTemplate
				val blockName = ctx.currentBlockName
				if (blockName != null) {
					ctx.capture {
						current.getBlock(ctx, blockName).eval(ctx)
					}
				} else {
					""
				}
			}
		}
	}

	val ALL by lazy { Dynamic.getStaticTypedFields<TeFunction>(javaClass) }
}