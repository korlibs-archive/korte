package com.soywiz.korte

import com.soywiz.korio.util.Dynamic

internal fun Any?.toDynamicString() = Dynamic.toString(this)
internal fun Any?.toDynamicBool() = Dynamic.toBool(this)
internal fun Any?.toDynamicInt() = Dynamic.toInt(this)
internal fun Any?.toDynamicList() = Dynamic.toList(this)
internal fun Any?.toDynamicIterable() = Dynamic.toIterable(this)
internal fun Any?.dynamicLength() = Dynamic.length(this)
suspend internal fun Any?.dynamicGet(key: Any?) = Dynamic.accessAny(this, key)
suspend internal fun Any?.dynamicSet(key: Any?, value: Any?) = Dynamic.setAny(this, key, value)
suspend internal fun Any?.dynamicCall(vararg args: Any?) = Dynamic.callAny(this, args.toList())
suspend internal fun Any?.dynamicCallMethod(methodName: Any?, vararg args: Any?) = Dynamic.callAny(this, methodName, args.toList())
suspend internal fun Any?.dynamicCastTo(target: Class<*>) = Dynamic.dynamicCast(this, target)
