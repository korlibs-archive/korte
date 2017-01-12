package com.soywiz.korte.filter

import com.soywiz.korte.Filter
import com.soywiz.korte.util.Dynamic

val FilterJoin = Filter("join") { subject, args -> Dynamic.toIterable(subject).map { Dynamic.toString(it) }.joinToString(Dynamic.toString(args[0])) }