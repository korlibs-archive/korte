package com.soywiz.korte.filter

import com.soywiz.korio.util.Dynamic
import com.soywiz.korio.util.quote
import com.soywiz.korte.Filter
import com.soywiz.korte.toDynamicString

val FilterQuote = Filter("quote") { subject, _ -> subject.toDynamicString().quote() }
