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