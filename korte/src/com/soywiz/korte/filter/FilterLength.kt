package com.soywiz.korte.filter

import com.soywiz.korio.util.Dynamic
import com.soywiz.korte.Filter

val FilterLength = Filter("length") { subject, _ -> subject.dynamicLength() }