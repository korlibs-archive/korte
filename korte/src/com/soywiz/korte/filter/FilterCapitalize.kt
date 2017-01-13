package com.soywiz.korte.filter

import com.soywiz.korio.util.Dynamic
import com.soywiz.korte.Filter

val FilterCapitalize = Filter("capitalize") { subject, _ -> subject.toDynamicString().toLowerCase().capitalize() }