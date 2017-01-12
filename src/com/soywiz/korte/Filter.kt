package com.soywiz.korte

data class Filter(val name: String, val eval: (subject: Any?, args: List<Any?>) -> Any?)
