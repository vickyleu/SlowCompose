package org.uooc.compose.utils.encode

import io.ktor.utils.io.core.toByteArray

expect fun ByteArray.md5(): ByteArray

fun String.md5(): String {
    val bytes = this.toByteArray()
    val hash = bytes.md5()
    val md5 = hash.joinToString("") {
        it.toInt().and(0xff).toString(16).padStart(2, '0')
    }
    return md5
}