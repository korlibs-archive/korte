package com.soywiz.korte.ktor

import com.soywiz.korio.file.*
import com.soywiz.korte.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.util.*
import kotlinx.coroutines.io.*

class KorteContent(
    val template: String,
    val model: Any?,
    val etag: String? = null,
    val contentType: ContentType = ContentType.Text.Html.withCharset(Charsets.UTF_8)
)

class Korte(private val config: Configuration) {
    class Configuration : TemplateConfig() {
        var templateLoader: suspend (String) -> String = { it }
        fun templateRoot(root: VfsFile) {
            templateLoader = { root[it].readString() }
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, Korte> {
        override val key: AttributeKey<Korte> = AttributeKey("korte")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): Korte {
            val config = Configuration().apply(configure)
            val feature = Korte(config)
            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Transform) { value ->
                if (value is KorteContent) {
                    val response = feature.process(value)
                    proceedWith(response)
                }
            }
            return feature
        }
    }

    private suspend fun process(content: KorteContent): KorteOutgoingContent {
        return KorteOutgoingContent(
            Template(config.templateLoader(content.template), config),
            content.model,
            content.etag,
            content.contentType
        )
    }

    private class KorteOutgoingContent(
        val template: Template,
        val model: Any?,
        etag: String?,
        override val contentType: ContentType
    ) : OutgoingContent.WriteChannelContent() {
        override suspend fun writeTo(channel: ByteWriteChannel) {
            template.prender(model).write { channel.writeStringUtf8(it) }
        }

        init {
            if (etag != null)
                versions += EntityTagVersion(etag)
        }
    }
}
