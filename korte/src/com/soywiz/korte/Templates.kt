@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.soywiz.korte

import com.soywiz.korio.util.AsyncCache
import com.soywiz.korio.vfs.VfsFile

class Templates(
	val root: VfsFile,
	val includes: VfsFile = root,
	val layouts: VfsFile = root,
	val config: TemplateConfig = TemplateConfig()
) {
	val cache = AsyncCache()

	suspend fun getInclude(name: String): Template = cache("include/$name") {
		val content = includes[name].readString()
		Template(this@Templates, content, config).init()
	}

	suspend fun getLayout(name: String): Template = cache("layout/$name") {
		val content = includes[name].readString()
		Template(this@Templates, content, config).init()
	}

	suspend operator fun get(name: String): Template = cache(name) {
		val content = root[name].readString()
		Template(this@Templates, content, config).init()
	}
}