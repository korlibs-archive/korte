package com.soywiz.korte

import com.soywiz.korio.async.*
import com.soywiz.korio.file.*
import com.soywiz.korte.util.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

open class Templates(
    var root: VfsFile,
    var includes: VfsFile = root,
    var layouts: VfsFile = root,
    val config: TemplateConfig = TemplateConfig(),
    var cache: Boolean = true
) {
    internal class AsyncCache {
        @PublishedApi
        internal val promises = LinkedHashMap<String, Deferred<*>>()

        fun invalidateAll() {
            promises.clear()
        }

        @Suppress("UNCHECKED_CAST")
        suspend operator fun <T> invoke(key: String, gen: suspend () -> T): T {
            return (promises.getOrPut(key) { asyncImmediately(coroutineContext) { gen() } } as Deferred<T>).await()
        }
    }

    @PublishedApi
    internal val tcache = AsyncCache()

    fun invalidateCache() {
        tcache.invalidateAll()
    }

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

    suspend fun render(name: String, args: Any?): String {
        return get(name).invoke(args)
    }

    suspend fun prender(name: String, vararg args: Pair<String, Any?>): AsyncTextWriterContainer {
        return get(name).prender(*args)
    }

    suspend fun prender(name: String, args: Any?): AsyncTextWriterContainer {
        return get(name).prender(args)
    }
}