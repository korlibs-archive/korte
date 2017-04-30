package com.soywiz.korte

import com.soywiz.korio.async.spawnAndForget
import com.soywiz.korio.coroutine.CoroutineContext
import com.soywiz.korio.vertx.vxResult
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.templ.TemplateEngine

class KorteVertxTemplateEngine(val coroutineContext: CoroutineContext, val templates: Templates) : TemplateEngine {
	override fun render(context: RoutingContext, templateFileName: String, handler: Handler<AsyncResult<Buffer>>): Unit {
		spawnAndForget(coroutineContext) {
			val result = vxResult {
				val str = templates.get(templateFileName).invoke()
				Buffer.buffer(str)
			}
			handler.handle(result)
		}
	}
}