package com.soywiz.korte

import com.soywiz.korte.internal.*

@Suppress("unused")
object DefaultFilters {
    val Capitalize = Filter("capitalize") { subject.toDynamicString().toLowerCase().capitalize() }
    val Join = Filter("join") {
        subject.toDynamicList().joinToString(args[0].toDynamicString()) { it.toDynamicString() }
    }
    val Length = Filter("length") { subject.dynamicLength() }
    val Lower = Filter("lower") { subject.toDynamicString().toLowerCase() }
    val Quote = Filter("quote") { subject.toDynamicString().quote() }
    val Raw = Filter("raw") { RawString(subject.toDynamicString()) }
    val Reverse =
        Filter("reverse") { (subject as? String)?.reversed() ?: subject.toDynamicList().reversed() }

    val Slice = Filter("slice") {
        val lengthArg = args.getOrNull(1)
        val start = args.getOrNull(0).toDynamicInt()
        val length = lengthArg?.toDynamicInt() ?: subject.dynamicLength()
        if (subject is String) {
            val str = subject.toDynamicString()
            str.slice(start.coerceIn(0, str.length) until (start + length).coerceIn(0, str.length))
        } else {
            val list = subject.toDynamicList()
            list.slice(start.coerceIn(0, list.size) until (start + length).coerceIn(0, list.size))
        }
    }

    val Sort = Filter("sort") {
        subject.toDynamicList().sortedBy { it.toDynamicString() }
    }
    val Trim = Filter("trim") { subject.toDynamicString().trim() }
    val Upper = Filter("upper") { subject.toDynamicString().toUpperCase() }
    val Merge = Filter("merge") {
        val arg = args.getOrNull(0)
        subject.toDynamicList() + arg.toDynamicList()
    }
    val JsonEncode = Filter("json_encode") {
        Json.stringify(subject)
    }
    val Format = Filter("format") {
        subject.toDynamicString().format(*(args.toTypedArray() as Array<out Any>))
    }
    // EXTRA from Kotlin
    val Chunked = Filter("chunked") {
        subject.toDynamicList().chunked(args[0].toDynamicInt())
    }

    val ALL = listOf(
        Capitalize,
        Join,
        Length,
        Lower,
        Quote,
        Raw,
        Reverse,
        Slice,
        Sort,
        Trim,
        Upper,
        Merge,
        JsonEncode,
        Format,
        Chunked
    )
}
