package com.soywiz.korte.dynamic

import com.soywiz.kds.*
import com.soywiz.korio.async.*
import com.soywiz.korio.util.*
import java.lang.reflect.*
import kotlin.reflect.*

open class JvmObjectMapper2 : ObjectMapper2() {
    class ClassReflectCache<T : Any>(val clazz: KClass<T>) {
        data class MyProperty(val name: String, val getter: Method? = null, val setter: Method? = null, val field: Field? = null)

        val jclass = clazz.java
        val methodsByName = jclass.allDeclaredMethods.associateBy { it.name }
        val fieldsByName = jclass.allDeclaredFields.associateBy { it.name }
        val potentialPropertyNamesFields = jclass.allDeclaredFields.map { it.name }
        val potentialPropertyNamesGetters = jclass.allDeclaredMethods.filter { it.name.startsWith("get") }.map { it.name.substring(3).decapitalize() }
        val potentialPropertyNames = (potentialPropertyNamesFields + potentialPropertyNamesGetters).toSet()
        val propByName = potentialPropertyNames.map { propName -> MyProperty(propName, methodsByName["get${propName.capitalize()}"], methodsByName["set${propName.capitalize()}"], fieldsByName[propName]) }.associateBy { it.name }
    }

    val KClass<*>.classInfo by WeakPropertyThis<KClass<*>, ClassReflectCache<*>> { ClassReflectCache(this) }

    override fun hasProperty(instance: Any, key: String): Boolean {
        return key in instance::class.classInfo.propByName
    }

    override fun hasMethod(instance: Any, key: String): Boolean {
        return instance::class.classInfo.methodsByName[key] != null
    }

    override suspend fun invokeAsync(type: KClass<Any>, instance: Any?, key: String, args: List<Any?>): Any? {
        val method = type.classInfo.methodsByName[key] ?: return null
        return method.invokeSuspend(instance, args)
    }

    override suspend fun set(instance: Any, key: Any?, value: Any?) {
        val prop = instance::class.classInfo.propByName[key] ?: return
        prop.setter?.invoke(instance, value)
    }

    override suspend fun get(instance: Any, key: Any?): Any? {
        val prop = instance::class.classInfo.propByName[key] ?: return null
        return prop.getter?.invoke(instance)
    }
}

actual val Mapper2: ObjectMapper2 = JvmObjectMapper2()