package com.soywiz.korte

import com.soywiz.korio.async.EventLoop
import com.soywiz.korio.async.EventLoopTest
import com.soywiz.korio.async.sync
import com.soywiz.korio.util.captureStdout
import com.soywiz.korte.BlockNode
import com.soywiz.korte.Tag
import org.junit.Assert
import org.junit.Test

class TemplateTest {
	val el = EventLoopTest().apply { EventLoop.impl = this }

	@Test fun testDummy() = sync {
		Assert.assertEquals("hello", (Template("hello"))(null))
	}

	@Test fun testSimple() = sync {
		Assert.assertEquals("hello soywiz", Template("hello {{ name }}")(mapOf("name" to "soywiz")))
		Assert.assertEquals("soywizsoywiz", Template("{{name}}{{ name }}")(mapOf("name" to "soywiz")))
	}

	@Test fun testFor() = sync {
		Assert.assertEquals("123", Template("{% for n in numbers %}{{ n }}{% end %}")(mapOf("numbers" to listOf(1, 2, 3))))
	}

	@Test fun testDebug() = sync {
		var result: String? = null
		val tpl = Template("a {% debug 'hello ' + name %} b")
		val stdout = captureStdout {
			result = tpl(mapOf("name" to "world"))
		}
		Assert.assertEquals("hello world", stdout.trim())
		Assert.assertEquals("a  b", result)
	}

	@Test fun testSimpleIf() = sync {
		Assert.assertEquals("true", Template("{% if cond %}true{% else %}false{% end %}")(mapOf("cond" to 1)))
		Assert.assertEquals("false", Template("{% if cond %}true{% else %}false{% end %}")(mapOf("cond" to 0)))
		Assert.assertEquals("true", Template("{% if cond %}true{% end %}")(mapOf("cond" to 1)))
		Assert.assertEquals("", Template("{% if cond %}true{% end %}")(mapOf("cond" to 0)))
	}

	@Test fun testEval() = sync {
		Assert.assertEquals("-5", Template("{{ -(1 + 4) }}")(null))
		Assert.assertEquals("false", Template("{{ 1 == 2 }}")(null))
		Assert.assertEquals("true", Template("{{ 1 < 2 }}")(null))
		Assert.assertEquals("true", Template("{{ 1 <= 1 }}")(null))
	}

	@Test fun testExists() = sync {
		Assert.assertEquals("false", Template("{% if prop %}true{% else %}false{% end %}")(null))
		Assert.assertEquals("true", Template("{% if prop %}true{% else %}false{% end %}")(mapOf("prop" to "any")))
		Assert.assertEquals("false", Template("{% if prop %}true{% else %}false{% end %}")(mapOf("prop" to "")))
	}

	@Test fun testForAccess() = sync {
		Assert.assertEquals("ZardBallesteros", Template("{% for n in persons %}{{ n.surname }}{% end %}")(mapOf("persons" to listOf(Person("Soywiz", "Zard"), Person("Carlos", "Ballesteros")))))
		Assert.assertEquals("ZardBallesteros", Template("{% for n in persons %}{{ n['sur'+'name'] }}{% end %}")(mapOf("persons" to listOf(Person("Soywiz", "Zard"), Person("Carlos", "Ballesteros")))))
		Assert.assertEquals("ZardBallesteros", Template("{% for nin in persons %}{{ nin['sur'+'name'] }}{% end %}")(mapOf("persons" to listOf(Person("Soywiz", "Zard"), Person("Carlos", "Ballesteros")))))
	}

	@Test fun testFilters() = sync {
		Assert.assertEquals("CARLOS", Template("{{ name|upper }}")(mapOf("name" to "caRLos")))
		Assert.assertEquals("carlos", Template("{{ name|lower }}")(mapOf("name" to "caRLos")))
		Assert.assertEquals("Carlos", Template("{{ name|capitalize }}")(mapOf("name" to "caRLos")))
		Assert.assertEquals("Carlos", Template("{{ (name)|capitalize }}")(mapOf("name" to "caRLos")))
		Assert.assertEquals("Carlos", Template("{{ 'caRLos'|capitalize }}")(null))
	}

	@Test fun testArrayLiterals() = sync {
		Assert.assertEquals("1234", Template("{% for n in [1, 2, 3, 4] %}{{ n }}{% end %}")(null))
		Assert.assertEquals("", Template("{% for n in [] %}{{ n }}{% end %}")(null))
		Assert.assertEquals("1, 2, 3, 4", Template("{{ [1, 2, 3, 4]|join(', ') }}")(null))
	}

	@Test fun testSet() = sync {
		Assert.assertEquals("1,2,3", Template("{% set a = [1,2,3] %}{{ a|join(',') }}")(null))
	}

	@Test fun testAccessGetter() = sync {
		val success = "success!"

		class Test1 {
			val a: String get() = "$success"
		}

		Assert.assertEquals("$success", Template("{{ test.a }}")(mapOf("test" to Test1())))
	}

	@Test fun testCustomTag() = sync {
		class CustomNode(val text: String) : BlockNode {
			override fun eval(context: Template.Context) = run { context.write("CUSTOM($text)") }
		}

		val CustomTag = Tag("custom", setOf(), null) {
			CustomNode(it.first().token.content)
		}

		Assert.assertEquals(
			"CUSTOM(test)CUSTOM(demo)",
			Template("{% custom test %}{% custom demo %}", Template.Config(extraTags = listOf(CustomTag))).invoke(null)
		)
	}

	data class Person(val name: String, val surname: String)
}