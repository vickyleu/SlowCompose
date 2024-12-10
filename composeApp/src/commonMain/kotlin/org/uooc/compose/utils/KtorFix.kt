package org.uooc.compose.utils

import io.ktor.client.request.forms.ChannelProvider
import io.ktor.client.request.forms.FormPart
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.http.escapeIfNeeded
import io.ktor.util.StringValues
import io.ktor.utils.io.InternalAPI
import kotlinx.io.Source
import io.ktor.utils.io.core.Input
import io.ktor.utils.io.core.copy
import io.ktor.utils.io.core.remaining

public fun formDataFix(block: FormBuilderFix.() -> Unit): List<PartData> {
    return formDataFix(*FormBuilderFix().apply(block).build().toTypedArray())
}

public fun formDataFix(vararg values: FormPart<*>): List<PartData> {
    val result = mutableListOf<PartData>()
    values.forEach { (key, value, headers) ->
        val partHeaders = HeadersBuilder().apply {
            if (headers.contains(HttpHeaders.ContentDisposition).not()) {
                append(
                    HttpHeaders.ContentDisposition,
                    "form-data; name=\"${key.escapeIfNeeded()}\""
                )
            }
            appendAll(headers.entries().map { (key, values) ->
                // 创建一个新的可变列表来存储处理后的值
                val newValues = mutableListOf<String>()
                values.forEach { value ->
                    val innerValue = value.split(";")
                    if (innerValue.isNotEmpty()) {
                        innerValue.joinToString("; ") {
                            val split = it.trim().split("=")
                            if (split.size == 2) {
                                val (k, v) = split
                                val quotedValue = if (v.startsWith("\"") && v.endsWith("\"")) {
                                    v.trim()
                                } else {
                                    "\"${v.trim()}\""
                                }
                                "${k.trim()}=$quotedValue"
                            } else {
                                it
                            }
                        }.apply {
                            newValues.add(this)
                        }
                    }
                }
                key to newValues.toList()
            }.toList().let {
                StringValues.build(caseInsensitiveName = true) {
                    it.forEach { (key, values) ->
                        this.appendAll(key, values)
                    }
                }
            })
        }
        val part = when (value) {
            is String -> PartData.FormItem(value, {}, partHeaders.build())
            is Number -> PartData.FormItem(value.toString(), {}, partHeaders.build())
            is Boolean -> PartData.FormItem(value.toString(), {}, partHeaders.build())
            is ByteArray -> {
                partHeaders.append(HttpHeaders.ContentLength, value.size.toString())
                PartData.BinaryItem({ io.ktor.utils.io.core.ByteReadPacket(value) }, {}, partHeaders.build())
            }

            is Source -> {
                partHeaders.append(HttpHeaders.ContentLength, value.remaining.toString())
                PartData.BinaryItem({ value.copy() }, { value.close() }, partHeaders.build())
            }

            is InputProvider -> {
                val size = value.size
                if (size != null) {
                    partHeaders.append(HttpHeaders.ContentLength, size.toString())
                }
                PartData.BinaryItem(value.block, {}, partHeaders.build())
            }

            is ChannelProvider -> {
                val size = value.size
                if (size != null) {
                    partHeaders.append(HttpHeaders.ContentLength, size.toString())
                }
                PartData.BinaryChannelItem(value.block, partHeaders.build())
            }
            else -> error("Unknown form content type: $value")
        }
        result += part
    }

    return result
}

/**
 * A form builder type used in the [formData] builder function.
 */
public class FormBuilderFix internal constructor() {
    private val parts = mutableListOf<FormPart<*>>()

    /**
     * Appends a pair [key]:[value] with optional [headers].
     */
    @InternalAPI
    public fun <T : Any> append(key: String, value: T, headers: Headers = Headers.Empty) {
        parts += FormPart(key, value, headers)
    }

    /**
     * Appends a pair [key]:[value] with optional [headers].
     */
    public fun append(key: String, value: String, headers: Headers = Headers.Empty) {
        parts += FormPart(key, value, headers)
    }

    /**
     * Appends a pair [key]:[value] with optional [headers].
     */
    public fun append(key: String, value: Number, headers: Headers = Headers.Empty) {
        parts += FormPart(key, value, headers)
    }

    /**
     * Appends a pair [key]:[value] with optional [headers].
     */
    public fun append(key: String, value: Boolean, headers: Headers = Headers.Empty) {
        parts += FormPart(key, value, headers)
    }

    /**
     * Appends a pair [key]:[value] with optional [headers].
     */
    public fun append(key: String, value: ByteArray, headers: Headers = Headers.Empty) {
        parts += FormPart(key, value, headers)
    }

    /**
     * Appends a pair [key]:[value] with optional [headers].
     */
    public fun append(key: String, value: InputProvider, headers: Headers = Headers.Empty) {
        parts += FormPart(key, value, headers)
    }

    /**
     * Appends a pair [key]:[InputProvider(block)] with optional [headers].
     */
    @Suppress("DEPRECATION")
    public fun appendInput(
        key: String,
        headers: Headers = Headers.Empty,
        size: Long? = null,
        block: () -> Input
    ) {
        parts += FormPart(key, InputProvider(size, block), headers)
    }

    /**
     * Appends a pair [key]:[value] with optional [headers].
     */
    public fun append(key: String, value: Source, headers: Headers = Headers.Empty) {
        parts += FormPart(key, value, headers)
    }

    /**
     * Appends a pair [key]:[values] with optional [headers].
     */
    public fun append(key: String, values: Iterable<String>, headers: Headers = Headers.Empty) {
        require(key.endsWith("[]")) {
            "Array parameter must be suffixed with square brackets ie `$key[]`"
        }
        values.forEach { value ->
            parts += FormPart(key, value, headers)
        }
    }

    /**
     * Appends a pair [key]:[values] with optional [headers].
     */
    public fun append(key: String, values: Array<String>, headers: Headers = Headers.Empty) {
        return append(key, values.asIterable(), headers)
    }

    /**
     * Appends a pair [key]:[ChannelProvider] with optional [headers].
     */
    public fun append(key: String, value: ChannelProvider, headers: Headers = Headers.Empty) {
        parts += FormPart(key, value, headers)
    }

    /**
     * Appends a form [part].
     */
    public fun <T : Any> append(part: FormPart<T>) {
        parts += part
    }

    internal fun build(): List<FormPart<*>> = parts
}
