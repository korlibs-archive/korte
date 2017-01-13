package com.soywiz.korte.filter

import com.soywiz.korio.util.Dynamic
import com.soywiz.korio.util.clamp
import com.soywiz.korte.Filter

val FilterSlice = Filter("slice") { subject, args ->
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
