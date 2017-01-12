package com.soywiz.korte

import com.soywiz.korio.util.AsyncCache
import com.soywiz.korio.vfs.VfsFile

class Templates(
	val root: VfsFile,
	val config: TemplateConfig = TemplateConfig()
) {
	val cache = AsyncCache()

	suspend operator fun get(name: String): Template = cache(name) {
		val content = root[name].readString()
		Template(this@Templates, content, config).init()
	}
}