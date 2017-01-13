package com.soywiz.korte

import com.soywiz.korio.async.EventLoop
import com.soywiz.korio.async.EventLoopTest
import com.soywiz.korio.async.sync
import com.soywiz.korio.vfs.MemoryVfsMix
import org.junit.Assert
import org.junit.Test

class TemplateInheritanceTest {
	val el = EventLoopTest().apply { EventLoop.impl = this }

	@Test
	fun simple() = sync {
		Assert.assertEquals(
			"hello",
			Templates(MemoryVfsMix(
				"a" to "hello"
			)).get("a")()
		)
	}

	@Test
	fun block() = sync {
		Assert.assertEquals(
			"hello",
			Templates(MemoryVfsMix(
				"a" to "{% block test %}hello{% end %}"
			)).get("a")()
		)
	}

	@Test
	fun extends() = sync {
		Assert.assertEquals(
			"b",
			Templates(MemoryVfsMix(
				"a" to """{% block test %}a{% end %}""",
				"b" to """{% extends "a" %}{% block test %}b{% end %}"""
			)).get("b")()
		)
	}

	@Test
	fun doubleExtends() = sync {
		Assert.assertEquals(
			"c",
			Templates(MemoryVfsMix(
				"a" to """{% block test %}a{% end %}""",
				"b" to """{% extends "a" %}{% block test %}b{% end %}""",
				"c" to """{% extends "b" %}{% block test %}c{% end %}"""
			)).get("c")()
		)
	}

	@Test
	fun doubleExtends2() = sync {
		Assert.assertEquals(
			"abcc",
			Templates(MemoryVfsMix(
				"a" to """{% block b1 %}a{% end %}{% block b2 %}a{% end %}{% block b3 %}a{% end %}{% block b4 %}a{% end %}""",
				"b" to """{% extends "a" %}{% block b2 %}b{% end %}{% block b4 %}b{% end %}""",
				"c" to """{% extends "b" %}{% block b3 %}c{% end %}{% block b4 %}c{% end %}"""
			)).get("c")()
		)
	}

	@Test
	fun include() = sync {
		Assert.assertEquals(
			"Hello World, Carlos.",
			Templates(MemoryVfsMix(
				"include" to """World""",
				"username" to """Carlos""",
				"main" to """Hello {% include "include" %}, {% include "username" %}."""
			)).get("main")()
		)
	}

	@Test
	fun jekyllLayout() = sync {
		Assert.assertEquals(
			"Hello Carlos.",
			Templates(MemoryVfsMix(
				"mylayout" to """Hello {{ content }}.""",
				"main" to """
					---
					layout: mylayout
					name: Carlos
					---
					{{ name }}
				""".trimIndent()
			)).get("main")()
		)
	}

	@Test
	fun jekyllLayoutEx() = sync {
		Assert.assertEquals(
			"<html><div>side</div><div><h1>Content</h1></div></html>",
			Templates(MemoryVfsMix(
				"root" to """
					<html>{{ content }}</html>
                """.trimIndent(),
				"twocolumns" to """
					---
					layout: root
					---
					<div>side</div><div>{{ content }}</div>
                """.trimIndent(),
				"main" to """
					---
					layout: twocolumns
					mycontent: Content
					---
					<h1>{{ mycontent }}</h1>
				""".trimIndent()
			)).get("main")()
		)
	}

	// @TODO:
	//@Test
	//fun operatorPrecedence() = sync {
	//	Assert.assertEquals("${2 + 3 * 5}", Template("{{ 1 + 2 * 3 }}")())
	//	Assert.assertEquals("${2 * 3 + 5}", Template("{{ 2 * 3 + 5 }}")())
	//}

	@Test
	fun operatorPrecedence() = sync {
		Assert.assertEquals("true", Template("{{ 1 in [1, 2] }}")())
		Assert.assertEquals("false", Template("{{ 3 in [1, 2] }}")())
	}
}