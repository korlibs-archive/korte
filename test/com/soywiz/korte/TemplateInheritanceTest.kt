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
			TemplateFactory(MemoryVfsMix(
				"a" to "hello"
			)).get("a")()
		)
	}

	@Test
	fun block() = sync {
		Assert.assertEquals(
			"hello",
			TemplateFactory(MemoryVfsMix(
				"a" to "{% block test %}hello{% end %}"
			)).get("a")()
		)
	}

	@Test
	fun extends() = sync {
		Assert.assertEquals(
			"b",
			TemplateFactory(MemoryVfsMix(
				"a" to """{% block test %}a{% end %}""",
				"b" to """{% extends "a" %}{% block test %}b{% end %}"""
			)).get("b")()
		)
	}

	@Test
	fun doubleExtends() = sync {
		Assert.assertEquals(
			"c",
			TemplateFactory(MemoryVfsMix(
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
			TemplateFactory(MemoryVfsMix(
				"a" to """{% block b1 %}a{% end %}{% block b2 %}a{% end %}{% block b3 %}a{% end %}{% block b4 %}a{% end %}""",
				"b" to """{% extends "a" %}{% block b2 %}b{% end %}{% block b4 %}b{% end %}""",
				"c" to """{% extends "b" %}{% block b3 %}c{% end %}{% block b4 %}c{% end %}"""
			)).get("c")()
		)
	}
}