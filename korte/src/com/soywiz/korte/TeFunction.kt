package com.soywiz.korte

import com.soywiz.korio.util.Dynamic

data class TeFunction(val name: String, val eval: Dynamic.Context.(args: List<Any?>, context: Template.EvalContext) -> Any?) {
	fun eval(args: List<Any?>, context: Template.EvalContext) = eval(Dynamic.contextInstance, args, context)
}
