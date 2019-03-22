package com.soywiz.korte

import com.soywiz.korte.dynamic.*

data class Filter(val name: String, val eval: suspend Ctx.() -> Any?) {
    class Ctx : DynamicContext {
        lateinit var context: Template.EvalContext
        var subject: Any? = null
        var args: List<Any?> = listOf()
    }
}
