package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.toDynamicString

val FilterUpper = Filter("upper") { subject, _ -> subject.toDynamicString().toUpperCase() }
