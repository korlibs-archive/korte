package com.soywiz.korte

import com.soywiz.korte.internal.*
import com.soywiz.korte.util.*

open class Templates(
    var root: TemplateProvider,
    var includes: TemplateProvider = root,
    var layouts: TemplateProvider = root,
    val config: TemplateConfig = TemplateConfig(),
    var cache: Boolean = true
) {
    @PublishedApi
    internal val tcache = AsyncCache()

    fun invalidateCache() {
        tcache.invalidateAll()
    }

    @PublishedApi
    internal suspend fun cache(name: String, callback: suspend () -> Template): Template = when {
        cache -> tcache(name) { callback() }
        else -> callback()
    }

    open suspend fun getInclude(name: String): Template = cache("include/$name") {
        Template(name, this@Templates, includes.getSure(name), config).init()
    }

    open suspend fun getLayout(name: String): Template = cache("layout/$name") {
        Template(name, this@Templates, layouts.getSure(name), config).init()
    }

    open suspend fun get(name: String): Template = cache("base/$name") {
        Template(name, this@Templates, root.getSure(name), config).init()
    }

    suspend fun render(name: String, vararg args: Pair<String, Any?>): String = get(name).invoke(*args)
    suspend fun render(name: String, args: Any?): String = get(name).invoke(args)
    suspend fun prender(name: String, vararg args: Pair<String, Any?>): AsyncTextWriterContainer =
        get(name).prender(*args)

    suspend fun prender(name: String, args: Any?): AsyncTextWriterContainer = get(name).prender(args)
}