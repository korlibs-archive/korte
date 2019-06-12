# Kotlin cORoutines Template Engine

![](https://raw.githubusercontent.com/korlibs/korlibs-logos/master/128/korte.png)

[![Build Status](https://travis-ci.org/korlibs/korte.svg?branch=master)](https://travis-ci.org/korlibs/korte)
[![Maven Version](https://img.shields.io/github/tag/korlibs/korte.svg?style=flat&label=maven)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22korte%22)

KorTE is an asynchronous templating engine for Multiplatform Kotlin 1.3+.

It is a non-strict super set of twig / django / atpl.js template engines and can support liquid templating engine as well with frontmatter.

It has out of the box support for [ktor](https://ktor.io/) and [vert.x](https://vertx.io/).

It works on JVM and JS out of the box. And on Native with untyped model data or by making the models implement the [DynamicType](https://github.com/korlibs/korte/blob/7461aa4b7dc496ff1c0e986cdb2c7843891ba325/korte/src/commonMain/kotlin/com/soywiz/korte/dynamic/DynamicType.kt#L61) interface.

Because asynchornity is in its name and soul, it allows to call *suspend*ing methods from within your templates.

## Documentation:

* <https://korlibs.soywiz.com/korte/>

## Live demo

* ACE: <https://korlibs.github.io/korte-samples/korte-sample-browser/web/>
* OLD: <https://korlibs.github.io/kor_samples/korte1/>

## Example

### `resources/views/_base.html`
```liquid
<html><head></head><body>
{% block content %}default content{% endblock %}
</body></html>
```

### `resources/views/_two_columns.html`
```liquid
{% extends "_base.html" %}
{% block content %}
    <div>{% block left %}default left column{% endblock %}</div>
    <div>{% block right %}default right column{% endblock %}</div>
{% endblock %}
```

### `resources/views/index.html`
```liquid
{% extends "_two_columns.html" %}
{% block left %}
    My left column. Hello {{ name|upper }}
{% endblock %}
{% block right %}
    My prefix {{ parent() }} with additional content
{% endblock %}
```

### `code.kt`

```kotlin
val renderer = Templates(ResourceTemplateProvider("views"), cache = true)
val output = templates.render("index.html", mapOf("name" to "world"))
println(output)

class ResourceTemplateProvider(private val basePath: String) : TemplateProvider {
     override suspend fun get(template: String): String? {
         return this::class.java.classLoader.getResource(Paths.get(basePath, template).toString()).readText()
     }
 }

```
