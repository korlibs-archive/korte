package com.soywiz.korte

import com.soywiz.kds.*
import com.soywiz.korio.lang.*

open class TemplateConfig(
    extraTags: List<Tag> = listOf(),
    extraFilters: List<Filter> = listOf(),
    extraFunctions: List<TeFunction> = listOf(),
    var charset: Charset = UTF8
) : Extra by Extra.Mixin() {
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