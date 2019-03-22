package com.soywiz.korte.dynamic

import com.soywiz.kds.*
import kotlin.reflect.*
import kotlin.reflect.full.*

open class JvmObjectMapper2 : ObjectMapper2() {
    class ClassReflectCache<T : Any>(val clazz: KClass<T>) {
        val propByName = clazz.memberProperties.associateBy { it.name }
        val methodsByName = clazz.memberFunctions.associateBy { it.name }
    }

    val KClass<*>.classInfo by WeakPropertyThis<KClass<*>, ClassReflectCache<*>> { ClassReflectCache(this) }

    override fun hasProperty(instance: Any, key: String): Boolean {
        return instance::class.classInfo.propByName[key] != null
    }

    override fun hasMethod(instance: Any, key: String): Boolean {
        return instance::class.classInfo.methodsByName[key] != null
    }

    override suspend fun invokeAsync(type: KClass<Any>, instance: Any?, key: String, args: List<Any?>): Any? {
        val method = type.classInfo.methodsByName[key] ?: return null
        return method.callSuspend(instance, *args.toTypedArray())
    }

    override suspend fun set(instance: Any, key: Any?, value: Any?) {
        (instance::class.classInfo.propByName[key] as? KMutableProperty1<Any, Any?>?)?.set(instance, value)
    }

    override suspend fun get(instance: Any, key: Any?): Any? {
        return (instance::class.classInfo.propByName[key] as? KProperty1<Any, Any?>)?.get(instance)
    }
}

actual val Mapper2: ObjectMapper2 = JvmObjectMapper2()