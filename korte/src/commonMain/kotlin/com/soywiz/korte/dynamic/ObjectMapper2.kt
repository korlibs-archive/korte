package com.soywiz.korte.dynamic

import kotlin.reflect.*

open class ObjectMapper2 {
    open fun hasProperty(instance: Any, key: String): Boolean = false
    open fun hasMethod(instance: Any, key: String): Boolean = false
    open suspend fun invokeAsync(type: KClass<Any>, instance: Any?, key: String, args: List<Any?>): Any? = null
    open suspend fun set(instance: Any, key: Any?, value: Any?) = Unit
    open suspend fun get(instance: Any, key: Any?): Any? = null
}

expect val Mapper2: ObjectMapper2
