package com.soywiz.korte.filter

import com.soywiz.korio.util.clamp
import com.soywiz.korio.util.quote
import com.soywiz.korte.Filter
import com.soywiz.korte.RawString

val FilterCapitalize = Filter("capitalize") { subject, _ -> subject.toDynamicString().toLowerCase().capitalize() }
val FilterJoin = Filter("join") { subject, args -> subject.toDynamicList().map { it.toDynamicString() }.joinToString(args[0].toDynamicString()) }
val FilterLength = Filter("length") { subject, _ -> subject.dynamicLength() }
val FilterLower = Filter("lower") { subject, _ -> subject.toDynamicString().toLowerCase() }
val FilterQuote = Filter("quote") { subject, _ -> subject.toDynamicString().quote() }
val FilterRaw = Filter("raw") { subject, _ -> RawString(subject.toDynamicString()) }
val FilterReverse = Filter("reverse") { subject, _ ->  (subject as? String)?.reversed() ?: subject.toDynamicList().reversed()  }
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
val FilterSort = Filter("sort") { subject, _ ->
	subject.toDynamicList().sortedBy { it.toDynamicString() }
}
val FilterTrim = Filter("trim") { subject, _ -> subject.toDynamicString().trim() }
val FilterUpper = Filter("upper") { subject, _ -> subject.toDynamicString().toUpperCase() }
