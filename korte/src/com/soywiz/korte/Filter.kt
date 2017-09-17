package com.soywiz.korte

import com.soywiz.korio.util.Dynamic

data class Filter(val name: String, val eval: suspend Dynamic.Context.(subject: Any?, args: List<Any?>, context: Template.EvalContext) -> Any?) {
}
