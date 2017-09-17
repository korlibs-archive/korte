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

	val ALL by lazy { Dynamic.getStaticTypedFields<TeFunction>(javaClass) }
}