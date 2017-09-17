package com.soywiz.korte

import com.soywiz.korio.async.*
import com.soywiz.korio.util.asyncCaptureStdout
import com.soywiz.korio.vfs.MemoryVfsMix
import org.junit.Assert
import org.junit.Test

class TemplateTest : BaseTest() {
	@Test fun testDummy() = syncTest {
		Assert.assertEquals("hello", (Template("hello"))(null))
	}

	@Test fun testSimple() = syncTest {
		Assert.assertEquals("hello soywiz", Template("hello {{ name }}")("name" to "soywiz"))
		Assert.assertEquals("soywizsoywiz", Template("{{name}}{{ name }}")("name" to "soywiz"))
	}

	@Test fun testFor() = syncTest {
		val tpl = Template("{% for n in numbers %}{{ n }}{% end %}")
		Assert.assertEquals("", tpl("numbers" to listOf<Int>()))
		Assert.assertEquals("123", tpl("numbers" to listOf(1, 2, 3)))
	}

	@Test fun testForAdv() = syncTest {
		val tpl = Template("{% for n in numbers %}{{ n }}:{{ loop.index0 }}:{{ loop.index }}:{{ loop.revindex }}:{{ loop.revindex0 }}:{{ loop.first }}:{{ loop.last }}:{{ loop.length }}{{ '\\n' }}{% end %}")
		Assert.assertEquals(
			"""
				a:0:1:2:3:true:false:3
				b:1:2:1:2:false:false:3
				c:2:3:0:1:false:true:3
			""".trimIndent().trim(),
			tpl("numbers" to listOf("a", "b", "c")).trim()
		)
	}

	@Test fun testForMap() = syncTest {
		val tpl = Template("{% for k, v in map %}{{ k }}:{{v}}{% end %}")
		Assert.assertEquals("a:10b:c", tpl("map" to mapOf("a" to 10, "b" to "c")))
	}

	@Test fun testForElse() = syncTest {
		val tpl = Template("{% for n in numbers %}{{ n }}{% else %}none{% end %}")
		Assert.assertEquals("123", tpl("numbers" to listOf(1, 2, 3)))
		Assert.assertEquals("none", tpl("numbers" to listOf<Int>()))
	}

	@Test fun testDebug() = syncTest {
		var result: String? = null
		val tpl = Template("a {% debug 'hello ' + name %} b")
		val stdout = asyncCaptureStdout {
			result = tpl("name" to "world")
		}
		Assert.assertEquals("hello world", stdout.trim())
		Assert.assertEquals("a  b", result)
	}

	@Test fun testSimpleIf() = syncTest {
		Assert.assertEquals("true", Template("{% if cond %}true{% else %}false{% end %}")("cond" to 1))
		Assert.assertEquals("false", Template("{% if cond %}true{% else %}false{% end %}")("cond" to 0))
		Assert.assertEquals("true", Template("{% if cond %}true{% end %}")("cond" to 1))
		Assert.assertEquals("", Template("{% if cond %}true{% end %}")("cond" to 0))
	}

	@Test fun testSimpleElseIf() = syncTest {
		val tpl = Template("{% if v == 1 %}one{% elseif v == 2 %}two{% elseif v < 5 %}less than five{% elseif v > 8 %}greater than eight{% else %}other{% end %}")
		Assert.assertEquals("one", tpl("v" to 1))
		Assert.assertEquals("two", tpl("v" to 2))
		Assert.assertEquals("less than five", tpl("v" to 3))
		Assert.assertEquals("less than five", tpl("v" to 4))
		Assert.assertEquals("other", tpl("v" to 5))
		Assert.assertEquals("other", tpl("v" to 6))
		Assert.assertEquals("greater than eight", tpl("v" to 9))
	}

	@Test fun testEval() = syncTest {
		Assert.assertEquals("-5", Template("{{ -(1 + 4) }}")(null))
		Assert.assertEquals("false", Template("{{ 1 == 2 }}")(null))
		Assert.assertEquals("true", Template("{{ 1 < 2 }}")(null))
		Assert.assertEquals("true", Template("{{ 1 <= 1 }}")(null))
	}

	@Test fun testExists() = syncTest {
		Assert.assertEquals("false", Template("{% if prop %}true{% else %}false{% end %}")(null))
		Assert.assertEquals("true", Template("{% if prop %}true{% else %}false{% end %}")("prop" to "any"))
		Assert.assertEquals("false", Template("{% if prop %}true{% else %}false{% end %}")("prop" to ""))
	}

	@Test fun testForAccess() = syncTest {
		Assert.assertEquals("ZardBallesteros", Template("{% for n in persons %}{{ n.surname }}{% end %}")("persons" to listOf(Person("Soywiz", "Zard"), Person("Carlos", "Ballesteros"))))
		Assert.assertEquals("ZardBallesteros", Template("{% for n in persons %}{{ n['sur'+'name'] }}{% end %}")("persons" to listOf(Person("Soywiz", "Zard"), Person("Carlos", "Ballesteros"))))
		Assert.assertEquals("ZardBallesteros", Template("{% for nin in persons %}{{ nin['sur'+'name'] }}{% end %}")("persons" to listOf(Person("Soywiz", "Zard"), Person("Carlos", "Ballesteros"))))
	}

	@Test fun testFilters() = syncTest {
		Assert.assertEquals("CARLOS", Template("{{ name|upper }}")("name" to "caRLos"))
		Assert.assertEquals("carlos", Template("{{ name|lower }}")("name" to "caRLos"))
		Assert.assertEquals("Carlos", Template("{{ name|capitalize }}")("name" to "caRLos"))
		Assert.assertEquals("Carlos", Template("{{ (name)|capitalize }}")("name" to "caRLos"))
		Assert.assertEquals("Carlos", Template("{{ 'caRLos'|capitalize }}")(null))
	}

	@Test fun testArrayLiterals() = syncTest {
		Assert.assertEquals("1234", Template("{% for n in [1, 2, 3, 4] %}{{ n }}{% end %}")(null))
		Assert.assertEquals("", Template("{% for n in [] %}{{ n }}{% end %}")(null))
		Assert.assertEquals("1, 2, 3, 4", Template("{{ [1, 2, 3, 4]|join(', ') }}")(null))
	}

	@Test fun testElvis() = syncTest {
		Assert.assertEquals("1", Template("{{ 1 ?: 2 }}")(null))
		Assert.assertEquals("2", Template("{{ 0 ?: 2 }}")(null))
	}

	@Test fun testTernary() = syncTest {
		Assert.assertEquals("2", Template("{{ 1 ? 2 : 3 }}")(null))
		Assert.assertEquals("3", Template("{{ 0 ? 2 : 3 }}")(null))
	}

	@Test fun testSet() = syncTest {
		Assert.assertEquals("1,2,3", Template("{% set a = [1,2,3] %}{{ a|join(',') }}")(null))
	}

	@Test fun testAccessGetter() = syncTest {
		val success = "success!"

		class Test1 {
			val a: String get() = success
		}

		Assert.assertEquals(success, Template("{{ test.a }}")("test" to Test1()))
	}

	@Test fun testCustomTag() = syncTest {
		class CustomNode(val text: String) : Block {
			override suspend fun eval(context: Template.EvalContext) = context.write("CUSTOM($text)")
		}

		val CustomTag = Tag("custom", setOf(), null) {
			CustomNode(chunks.first().tag.content)
		}

		Assert.assertEquals(
			"CUSTOM(test)CUSTOM(demo)",
			Template("{% custom test %}{% custom demo %}", TemplateConfig(extraTags = listOf(CustomTag))).invoke(null)
		)
	}

	@Test fun testSlice() = syncTest {
		val map = hashMapOf("v" to listOf(1, 2, 3, 4))
		Assert.assertEquals("[1, 2, 3, 4]", Template("{{ v }}")(map))
		Assert.assertEquals("[2, 3, 4]", Template("{{ v|slice(1) }}")(map))
		Assert.assertEquals("[2, 3]", Template("{{ v|slice(1, 2) }}")(map))
		Assert.assertEquals("ello", Template("{{ v|slice(1) }}")(mapOf("v" to "hello")))
		Assert.assertEquals("el", Template("{{ v|slice(1, 2) }}")(mapOf("v" to "hello")))
	}

	@Test fun testReverse() = syncTest {
		val map = hashMapOf("v" to listOf(1, 2, 3, 4))
		Assert.assertEquals("[4, 3, 2, 1]", Template("{{ v|reverse }}")(map))
		Assert.assertEquals("olleh", Template("{{ v|reverse }}")(mapOf("v" to "hello")))
		Assert.assertEquals("le", Template("{{ v|slice(1, 2)|reverse }}")(mapOf("v" to "hello")))
	}

	@Test fun testObject() = syncTest {
		Assert.assertEquals("""{&quot;foo&quot;: 1, &quot;bar&quot;: 2}""", Template("{{ { 'foo': 1, 'bar': 2 } }}")())
	}

	@Test fun testEscape() = syncTest {
		Assert.assertEquals("<b>&lt;a&gt;</b>", Template("<b>{{ a }}</b>")("a" to "<a>"))
		Assert.assertEquals("<b><a></b>", Template("<b>{{ a|raw }}</b>")("a" to "<a>"))
		Assert.assertEquals("<b>&lt;A&gt;</b>", Template("<b>{{ a|raw|upper }}</b>")("a" to "<a>"))
		Assert.assertEquals("<b><A></b>", Template("<b>{{ a|upper|raw }}</b>")("a" to "<a>"))
	}

	@Test fun testTrim() = syncTest {
		Assert.assertEquals("""a  1  b""", Template("a  {{ 1 }}  b")())
		Assert.assertEquals("""a1  b""", Template("a  {{- 1 }}  b")())
		Assert.assertEquals("""a  1b""", Template("a  {{ 1 -}}  b")())
		Assert.assertEquals("""a1b""", Template("a  {{- 1 -}}  b")())

		Assert.assertEquals("""a     b""", Template("a  {% set a=1 %}   b")())
		Assert.assertEquals("""a   b""", Template("a  {%- set a=1 %}   b")())
		Assert.assertEquals("""a  b""", Template("a  {% set a=1 -%}   b")())
		Assert.assertEquals("""ab""", Template("a  {%- set a=1 -%}   b")())
	}

	@Test fun testOperatorPrecedence() = syncTest {
		Assert.assertEquals("${4 + 5 * 7}", Template("{{ 4+5*7 }}")())
		Assert.assertEquals("${4 * 5 + 7}", Template("{{ 4*5+7 }}")())
	}

	@Test fun testOperatorPrecedence2() = syncTest {
		Assert.assertEquals("${(4 + 5) * 7}", Template("{{ (4+5)*7 }}")())
		Assert.assertEquals("${(4 * 5) + 7}", Template("{{ (4*5)+7 }}")())
		Assert.assertEquals("${4 + (5 * 7)}", Template("{{ 4+(5*7) }}")())
		Assert.assertEquals("${4 * (5 + 7)}", Template("{{ 4*(5+7) }}")())
	}

	@Test fun testOperatorPrecedence3() = syncTest {
		Assert.assertEquals("${ -(4 + 5) }", Template("{{ -(4+5) }}")())
		Assert.assertEquals("${ +(4 + 5) }", Template("{{ +(4+5) }}")())
	}

	@Test fun testFrontMatter() = syncTest {
		Assert.assertEquals(
			"""hello""",
			Template(
				"""
					---
					title: hello
					---
					{{ title }}
				""".trimIndent()
			)()
		)
	}

	@Test fun testSuspendClass() = syncTest {
		class Test {
			suspend fun a(): Int {
				val r = executeInWorker { 1 }
				return r + 7
			}
		}

		Assert.assertEquals("""8""", Template("{{ v.a }}")("v" to Test()))
	}

	//@Test fun testStringInterpolation() = sync {
	//	Assert.assertEquals("a2b", Template("{{ \"a#{7 - 5}b\" }}")())
	//}

	@Test fun testConcatOperator() = syncTest {
		Assert.assertEquals("12", Template("{{ 1 ~ 2 }}")())
	}

	@Test fun testUnknownFilter() = syncTest {
		expectException("Unknown filter 'unknownFilter'") { Template("{{ 'a'|unknownFilter }}")() }
	}

	@Test fun testMissingFilterName() = syncTest {
		expectException("Missing filter name") { Template("{{ 'a'| }}")() }
	}

	data class Person(val name: String, val surname: String)

	@Test fun testImportMacros() = syncTest {
		val templates = Templates(MemoryVfsMix(
				"root.html" to "{% import '_macros.html' as macros %}{{ macros.putUserLink('hello') }}",
				"_macros.html" to "{% macro putUserLink(user) %}<a>{{ user }}</a>{% endmacro %}"
		))

		Assert.assertEquals("<a>hello</a>", templates.get("root.html").invoke(hashMapOf<Any, Any?>()))
	}
}