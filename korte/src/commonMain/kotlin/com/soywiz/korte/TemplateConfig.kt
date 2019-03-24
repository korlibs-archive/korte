package com.soywiz.korte

open class TemplateConfig(
    extraTags: List<Tag> = listOf(),
    extraFilters: List<Filter> = listOf(),
    extraFunctions: List<TeFunction> = listOf()
) {
    val extra = LinkedHashMap<String, Any>()

    val integratedFunctions = DefaultFunctions.ALL
    val integratedFilters = DefaultFilters.ALL
    val integratedTags = DefaultTags.ALL

    private val allFunctions = integratedFunctions + extraFunctions
    private val allTags = integratedTags + extraTags
    private val allFilters = integratedFilters + extraFilters

    val tags = hashMapOf<String, Tag>().apply {
        for (tag in allTags) {
            this[tag.name] = tag
            for (alias in tag.aliases) this[alias] = tag
        }
    }

    val filters = hashMapOf<String, Filter>().apply {
        for (filter in allFilters) this[filter.name] = filter
    }

    val functions = hashMapOf<String, TeFunction>().apply {
        for (func in allFunctions) this[func.name] = func
    }

    fun register(vararg its: Tag) = this.apply { for (it in its) tags[it.name] = it }
    fun register(vararg its: Filter) = this.apply { for (it in its) filters[it.name] = it }
    fun register(vararg its: TeFunction) = this.apply { for (it in its) functions[it.name] = it }
}

open class TemplateConfigWithTemplates(
    extraTags: List<Tag> = listOf(),
    extraFilters: List<Filter> = listOf(),
    extraFunctions: List<TeFunction> = listOf()
) : TemplateConfig(extraTags, extraFilters, extraFunctions) {
    var templates = Templates(TemplateProvider(mapOf()), config = this)
    fun cache(value: Boolean) = this.apply { templates.cache = value }
    fun root(root: TemplateProvider, includes: TemplateProvider = root, layouts: TemplateProvider = root) =
        this.apply {
            templates.root = root
            templates.includes = includes
            templates.layouts = layouts
        }
}
