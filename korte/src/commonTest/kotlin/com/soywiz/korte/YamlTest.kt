package com.soywiz.korte

import com.soywiz.korte.internal.Yaml
import kotlin.test.Test
import kotlin.test.assertEquals

class YamlTest {
    @Test
    fun test() {
        val yamlStr = """
        layout: post
        permalink: /lorem-ipsum/
        title: "Lorem Ipsum"
        feature_image: "/images/2019/lorem_ipsum.jpg"
        tags: [lorem_ipsum]
        date: 2019-10-07 00:00:00 
        """.trimIndent()
        //println(Yaml.tokenize(yamlStr))
        assertEquals(
            mapOf(
                "layout" to "post",
                "permalink" to "/lorem-ipsum/",
                "title" to "Lorem Ipsum",
                "feature_image" to "/images/2019/lorem_ipsum.jpg",
                "tags" to listOf("lorem_ipsum"),
                "date" to "2019-10-07 00:00:00"
            ),
            Yaml.decode(yamlStr)
        )
    }
}
