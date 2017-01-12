package com.soywiz.korte.filter

import com.soywiz.korio.util.clamp
import com.soywiz.korte.Filter
import com.soywiz.korte.util.Dynamic

val FilterSlice = Filter("slice") { subject, args ->
	val lengthArg = args.getOrNull(1)
	val start = Dynamic.toInt(args.getOrNull(0))
	val length = if (lengthArg != null) Dynamic.toInt(lengthArg) else Dynamic.length(subject)
	if (subject is String) {
		val str = Dynamic.toString(subject)
		str.slice(start.clamp(0, str.length) until (start + length).clamp(0, str.length))
	} else {
		val list = Dynamic.toList(subject)
		list.slice(start.clamp(0, list.size) until (start + length).clamp(0, list.size))
	}
}
