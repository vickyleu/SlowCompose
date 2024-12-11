package org.uooc.compose.component

import platform.Foundation.NSURL

actual class ComposeUri {
    private lateinit var uri: NSURL

    actual companion object {
        actual fun fromUrl(url: String): ComposeUri {
            val uri = ComposeUri()
            uri.uri = NSURL.URLWithString(URLString = url)!!
            return uri
        }
    }

    actual val scheme: String
        get() = uri.scheme!!
    actual val host: String
        get() = uri.host!!
    actual val path: String
        get() = uri.path!!
    actual val port: Int
        get() = uri.port!!.integerValue.toInt()
    actual val query: String
        get() = uri.query!!
}