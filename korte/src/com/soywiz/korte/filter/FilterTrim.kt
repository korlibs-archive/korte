package com.soywiz.korte.filter

import com.soywiz.korio.util.Dynamic
import com.soywiz.korte.Filter

val FilterTrim = Filter("trim") { subject, _ -> subject.toDynamicString().trim() }