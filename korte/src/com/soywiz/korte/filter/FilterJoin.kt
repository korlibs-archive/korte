package com.soywiz.korte.filter

import com.soywiz.korio.util.Dynamic
import com.soywiz.korte.Filter

val FilterJoin = Filter("join") { subject, args -> subject.toDynamicList().map { it.toDynamicString() }.joinToString(args[0].toDynamicString()) }