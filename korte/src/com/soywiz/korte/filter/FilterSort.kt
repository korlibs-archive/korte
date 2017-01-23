package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.toDynamicList
import com.soywiz.korte.toDynamicString

val FilterSort = Filter("sort") { subject, _ ->
	subject.toDynamicList().sortedBy { it.toDynamicString() }
}
