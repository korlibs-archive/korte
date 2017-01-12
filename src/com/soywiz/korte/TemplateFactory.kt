package com.soywiz.korte

import com.soywiz.korio.util.AsyncCache
import com.soywiz.korio.vfs.VfsFile

class TemplateFactory(
	val root: VfsFile,
	val config: Template.Config = Template.Config()
) {
	val cache = AsyncCache()

	suspend operator fun get(name: String): Template = cache(name) {
		val content = root[name].readString()
		Template(this@TemplateFactory, content, config).init()
	}
}