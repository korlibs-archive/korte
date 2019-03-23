# Kotlin cORoutines Template Engine

![](https://raw.githubusercontent.com/korlibs/korlibs-logos/master/128/korte.png)

[![Build Status](https://travis-ci.org/korlibs/korte.svg?branch=master)](https://travis-ci.org/korlibs/korte)
[![Maven Version](https://img.shields.io/github/tag/korlibs/korte.svg?style=flat&label=maven)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22korte%22)

KorTE is a asynchronous template engine for Multiplatform Kotlin 1.3.

It is a non-strict super set of twig / django / atpl.js template engines and can support liquid templaet engine too with frontmatter.

It has out of the box support for ktor and vert.x.

It works on JVM and JS out of the box. But can also work on Native when using untyped model data or making models to implement the DynamicType interface.

It allows to call suspend methods from within templates.

## Documentation:

* <https://korlibs.soywiz.com/korte/>

## Live demo (old version)

* <https://korlibs.github.io/kor_samples/korte1/>

## Example

### `_base.html`
```liquid
<html><head></head><body>
{% block content %}default content{% endblock %}
</body></html>
```

### `_two_columns.html`
```liquid
{% extends "_base.html" %}
{% block content %}
    <div>{% block left %}default left column{% endblock %}</div>
    <div>{% block right %}default right column{% endblock %}</div>
{% endblock %}
```

### `index.html`
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
val templates = Templates(resourcesVfs)
println(templates.render("index.html", mapOf("name" to "world")))
```