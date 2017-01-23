package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.toDynamicString

val FilterCapitalize = Filter("capitalize") { subject, _ -> subject.toDynamicString().toLowerCase().capitalize() }