package com.soywiz.korte

import com.soywiz.korio.util.AsyncCache
import com.soywiz.korio.vfs.VfsFile

class TemplateFactoryContext(
	val root: VfsFile,
	val config: com.soywiz.korte.Template.Config
) {
	val cache = AsyncCache()

	suspend operator fun get(name: String): com.soywiz.korte.Template = cache(name) {
		val content = root[name].readString()
		com.soywiz.korte.Template(this@TemplateFactoryContext, content, config)
	}
}