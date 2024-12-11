package org.uooc.compose.component

expect class ComposeUri private constructor() {
    companion object {
        fun fromUrl(url: String): ComposeUri
    }
    val scheme: String
    val host: String
    val path:String
    val port: Int
    val query: String
}