package org.uooc.compose.utils

import coil3.PlatformContext

expect object UrlLauncher {
    fun openUrl(context: PlatformContext, url: String)
}