package com.soywiz.korte.filter

import com.soywiz.korte.Filter

val FilterUpper = Filter("upper") { subject, _ -> subject.toDynamicString().toUpperCase() }
