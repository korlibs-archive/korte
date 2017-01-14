package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.toDynamicList
import com.soywiz.korte.toDynamicString

val FilterJoin = Filter("join") { subject, args -> subject.toDynamicList().map { it.toDynamicString() }.joinToString(args[0].toDynamicString()) }