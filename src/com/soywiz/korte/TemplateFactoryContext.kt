package com.soywiz.korte

import com.soywiz.korio.util.AsyncCache
import com.soywiz.korio.vfs.VfsFile

class TemplateFactoryContext(
	val root: VfsFile,
	val config: Template.Config
) {
	val cache = AsyncCache()

	suspend operator fun get(name: String): Template = cache(name) {
		val content = root[name].readString()
		Template(this@TemplateFactoryContext, content, config)
	}
}