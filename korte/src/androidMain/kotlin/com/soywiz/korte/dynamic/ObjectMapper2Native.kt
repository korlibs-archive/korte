package com.soywiz.korte.dynamic

import com.soywiz.kds.*
import com.soywiz.korio.async.*
import com.soywiz.korio.util.*
import java.lang.reflect.*
import kotlin.reflect.*
import kotlinx.coroutines.*
import java.lang.reflect.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

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


    private suspend fun Method.invokeSuspend(obj: Any?, args: List<Any?>): Any? {
        val method = this@invokeSuspend
        val cc = coroutineContext

        val lastParam = method.parameterTypes.lastOrNull()
        val margs = java.util.ArrayList(args)
        var deferred: CompletableDeferred<Any?>? = null

        if (lastParam != null && lastParam.isAssignableFrom(Continuation::class.java)) {
            deferred = CompletableDeferred<Any?>(Job())
            margs += deferred.toContinuation(cc)
        }
        val result = method.invoke(obj, *margs.toTypedArray())
        return if (result == COROUTINE_SUSPENDED) {
            deferred?.await()
        } else {
            result
        }
    }

    private fun <T> CompletableDeferred<T>.toContinuation(context: CoroutineContext, job: Job? = null): Continuation<T> {
        val deferred = CompletableDeferred<T>(job)
        return object : Continuation<T> {
            override val context: CoroutineContext = context

            override fun resumeWith(result: Result<T>) {
                val exception = result.exceptionOrNull()
                if (exception != null) {
                    deferred.completeExceptionally(exception)
                } else {
                    deferred.complete(result.getOrThrow())
                }
            }
        }
    }
}

actual val Mapper2: ObjectMapper2 = JvmObjectMapper2()
