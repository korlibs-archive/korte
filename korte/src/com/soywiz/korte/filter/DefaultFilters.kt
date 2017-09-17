package com.soywiz.korte.filter

import com.soywiz.korio.serialization.json.Json
import com.soywiz.korio.util.allDeclaredFields
import com.soywiz.korio.util.clamp
import com.soywiz.korio.util.quote
import com.soywiz.korte.Filter
import com.soywiz.korte.RawString
import java.lang.reflect.Modifier

@Suppress("unused")
object DefaultFilters {
	@JvmField val Capitalize = Filter("capitalize") { subject, _ -> subject.toDynamicString().toLowerCase().capitalize() }
	@JvmField val Join = Filter("join") { subject, args -> subject.toDynamicList().map { it.toDynamicString() }.joinToString(args[0].toDynamicString()) }
	@JvmField val Length = Filter("length") { subject, _ -> subject.dynamicLength() }
	@JvmField val Lower = Filter("lower") { subject, _ -> subject.toDynamicString().toLowerCase() }
	@JvmField val Quote = Filter("quote") { subject, _ -> subject.toDynamicString().quote() }
	@JvmField val Raw = Filter("raw") { subject, _ -> RawString(subject.toDynamicString()) }
	@JvmField val Reverse = Filter("reverse") { subject, _ -> (subject as? String)?.reversed() ?: subject.toDynamicList().reversed() }
	@JvmField val Slice = Filter("slice") { subject, args ->
		val lengthArg = args.getOrNull(1)
		val start = args.getOrNull(0).toDynamicInt()
		val length = lengthArg?.toDynamicInt() ?: subject.dynamicLength()
		if (subject is String) {
			val str = subject.toDynamicString()
			str.slice(start.clamp(0, str.length) until (start + length).clamp(0, str.length))
		} else {
			val list = subject.toDynamicList()
			list.slice(start.clamp(0, list.size) until (start + length).clamp(0, list.size))
		}
	}
	@JvmField val Sort = Filter("sort") { subject, _ ->
		subject.toDynamicList().sortedBy { it.toDynamicString() }
	}
	@JvmField val Trim = Filter("trim") { subject, _ -> subject.toDynamicString().trim() }
	@JvmField val Upper = Filter("upper") { subject, _ -> subject.toDynamicString().toUpperCase() }
	@JvmField val Merge = Filter("merge") { subject, args ->
		val arg = args.getOrNull(0)
		subject.toDynamicList() + arg.toDynamicList()
	}
	@JvmField val JsonEncode = Filter("json_encode") { subject, _ ->
		Json.encode(subject)
	}
	@JvmField val Format = Filter("format") { subject, args ->
		subject.toString().format(*args.toTypedArray())
	}

	val ALL by lazy {
		val list = arrayListOf<Filter>()
		for (field in javaClass.allDeclaredFields) {
			if (Modifier.isStatic(field.modifiers) && field.type == Filter::class.java) {
				list += field.get(null) as Filter
			}
		}
		list.toList()
	}
}
