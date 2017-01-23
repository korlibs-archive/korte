package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.RawString
import com.soywiz.korte.toDynamicString

val FilterRaw = Filter("raw") { subject, _ ->
	RawString(subject.toDynamicString())
}
