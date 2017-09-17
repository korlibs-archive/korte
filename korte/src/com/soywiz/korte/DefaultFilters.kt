package com.soywiz.korte

import com.soywiz.korio.serialization.json.Json
import com.soywiz.korio.util.Dynamic
import com.soywiz.korio.util.clamp
import com.soywiz.korio.util.quote

@Suppress("unused")
object DefaultFilters {
	@JvmStatic
	val Capitalize = Filter("capitalize") { subject, _, _ -> subject.toDynamicString().toLowerCase().capitalize() }

	@JvmStatic
	val Join = Filter("join") { subject, args, _ -> subject.toDynamicList().map { it.toDynamicString() }.joinToString(args[0].toDynamicString()) }

	@JvmStatic
	val Length = Filter("length") { subject, _, _ -> subject.dynamicLength() }

	@JvmStatic
	val Lower = Filter("lower") { subject, _, _ -> subject.toDynamicString().toLowerCase() }

	@JvmStatic
	val Quote = Filter("quote") { subject, _, _ -> subject.toDynamicString().quote() }

	@JvmStatic
	val Raw = Filter("raw") { subject, _, _ -> RawString(subject.toDynamicString()) }

	@JvmStatic
	val Reverse = Filter("reverse") { subject, _, _ -> (subject as? String)?.reversed() ?: subject.toDynamicList().reversed() }

	@JvmStatic
	val Slice = Filter("slice") { subject, args, _ ->
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

	@JvmStatic
	val Sort = Filter("sort") { subject, _, _ ->
		subject.toDynamicList().sortedBy { it.toDynamicString() }
	}

	@JvmStatic
	val Trim = Filter("trim") { subject, _, _ -> subject.toDynamicString().trim() }

	@JvmStatic
	val Upper = Filter("upper") { subject, _, _ -> subject.toDynamicString().toUpperCase() }

	@JvmStatic
	val Merge = Filter("merge") { subject, args, _ ->
		val arg = args.getOrNull(0)
		subject.toDynamicList() + arg.toDynamicList()
	}

	@JvmStatic
	val JsonEncode = Filter("json_encode") { subject, _, _ ->
		Json.encode(subject)
	}

	@JvmStatic
	val Format = Filter("format") { subject, args, _ ->
		subject.toString().format(*args.toTypedArray())
	}

	val ALL by lazy { Dynamic.getStaticTypedFields<Filter>(javaClass) }
}
