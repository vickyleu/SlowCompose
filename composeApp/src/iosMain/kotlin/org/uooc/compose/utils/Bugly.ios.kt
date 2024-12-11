package org.uooc.compose.utils

import what.the.fuck.Bugly.BuglyConfig
import coil3.PlatformContext
import com.multiplatform.webview.web.NativeWebView
import com.multiplatform.webview.web.WebViewNavigator
import org.uooc.compose.BuildKonfig
import org.uooc.compose.base.BuildConfigImpl
import org.uooc.compose.base.getAppVersionName

actual class Bugly private actual constructor() {
    private constructor(context: PlatformContext, appid: String) : this() {
        val config = BuglyConfig()
        config.setConsolelogEnable(true)
        config.setChannel("AppStore")
        config.setVersion(context.getAppVersionName())
        config.setDebugMode(BuildConfigImpl.isDebug)
//        config.setDeviceIdentifier("userdefinedId")

        what.the.fuck.Bugly.Bugly.startWithAppId(appid,developmentDevice=true,config=config)
    }
    actual companion object {
        private var instance: Bugly? = null

        actual fun init(
            context: PlatformContext,
            appid: String
        ): Bugly {
           return Bugly(context, appid).apply {
                instance = this
           }
        }

        actual fun setWebview(webView: NativeWebView, navigator: WebViewNavigator) {
            instance?.let {
                // CrashReport.setJavascriptMonitor(webView, true)
            }
        }

        actual fun releaseWebview(webView: NativeWebView) {
            instance?.let {
                // CrashReport.setJavascriptMonitor(webView, false)
            }
        }

        actual fun setUserIdentifier(userId: String) {
            instance?.let {
                what.the.fuck.Bugly.Bugly.setUserIdentifier(userId)
                // CrashReport.setUserId(userId)
            }
        }
    }
}