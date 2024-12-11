package org.uooc.compose.utils

import coil3.PlatformContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object UrlLauncher {
    actual fun openUrl(context: PlatformContext, url: String) {
        NSURL.URLWithString(URLString = url)?.let {
            if(UIApplication.sharedApplication.canOpenURL(it)){
                UIApplication.sharedApplication.openURL(it)
            }
        }
    }
}