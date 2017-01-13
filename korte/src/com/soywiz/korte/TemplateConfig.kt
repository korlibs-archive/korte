package com.soywiz.korte

import com.soywiz.korte.filter.*
import com.soywiz.korte.tag.*

class TemplateConfig(
	extraTags: List<Tag> = listOf(),
	extraFilters: List<Filter> = listOf()
    //parsePlugin: List<ParsePlugin>
) {
	val integratedFilters = listOf(
		FilterLength, FilterCapitalize, FilterUpper,
		FilterLower, FilterTrim, FilterQuote, FilterJoin,
		FilterSlice, FilterReverse
	)

	val integratedTags = listOf(
		TagEmpty, TagIf, TagFor, SetTag, CaptureTag,
		TagDebug,
		TagBlock, TagExtends, TagInclude
	)

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
}