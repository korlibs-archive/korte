package com.soywiz.korte

import com.soywiz.korio.async.*
import com.soywiz.korio.file.*
import com.soywiz.korte.util.*

open class Templates(
    var root: VfsFile,
    var includes: VfsFile = root,
    var layouts: VfsFile = root,
    val config: TemplateConfig = TemplateConfig()
) {
    var cache = true

    @PublishedApi
    internal val tcache = AsyncCache()

    @PublishedApi
    internal suspend fun cache(name: String, callback: suspend () -> Template): Template {
        return when {
            cache -> tcache(name) { callback() }
            else -> callback()
        }
    }

    open suspend fun getInclude(name: String): Template = cache("include/$name") {
        val content = includes[name].readString()
        Template(name, this@Templates, content, config).init()
    }

    open suspend fun getLayout(name: String): Template = cache("layout/$name") {
        val content = layouts[name].readString()
        Template(name, this@Templates, content, config).init()
    }

    //suspend operator fun get(name: String): Template = cache(name) { // @TODO: Unsupported operator. Re-enable when this limitation is lifted.
    open suspend fun get(name: String): Template = cache(name) {
        val content = root[name].readString()
        Template(name, this@Templates, content, config).init()
    }

    suspend fun render(name: String, vararg args: Pair<String, Any?>): String {
        return get(name).invoke(*args)
    }

    suspend fun render(name: String, args: Map<String, Any?>): String {
        return get(name).invoke(HashMap(args))
    }

    suspend fun prender(name: String, vararg args: Pair<String, Any?>): AsyncTextWriterContainer {
        return get(name).prender(*args)
    }

    suspend fun prender(name: String, args: Map<String, Any?>): AsyncTextWriterContainer {
        return get(name).prender(args)
    }
}