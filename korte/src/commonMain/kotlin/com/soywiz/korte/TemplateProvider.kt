package com.soywiz.korte

interface TemplateProvider {
	class NotFoundException(val template: String) : RuntimeException("Can't find template '$template'")

	suspend fun get(template: String): String?
}

suspend fun TemplateProvider.getSure(template: String) = get(template)
    ?: throw TemplateProvider.NotFoundException(template)

fun TemplateProvider(map: Map<String, String>): TemplateProvider = object : TemplateProvider {
	override suspend fun get(template: String): String? = map[template]
}

fun TemplateProvider(vararg map: Pair<String, String>): TemplateProvider = TemplateProvider(map.toMap())
