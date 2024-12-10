package org.uooc.compose.utils

import coil3.PlatformContext
import com.multiplatform.webview.web.NativeWebView
import com.multiplatform.webview.web.WebViewNavigator

expect class Bugly private constructor() {
    companion object {
        fun init(context: PlatformContext, appid: String): Bugly
        fun setUserIdentifier(userId: String)
        fun setWebview(webView: NativeWebView, navigator: WebViewNavigator)
        fun releaseWebview(webView: NativeWebView)
    }
}