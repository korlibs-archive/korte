package com.soywiz.korte

import com.soywiz.korio.util.Dynamic

data class Filter(val name: String, val eval: Dynamic.Context.(subject: Any?, args: List<Any?>) -> Any?) {
}
