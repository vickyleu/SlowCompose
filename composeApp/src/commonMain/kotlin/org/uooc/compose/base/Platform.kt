package org.uooc.compose.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import coil3.PlatformContext
import com.dokar.sonner.ToasterState
import com.github.jing332.filepicker.base.FileImpl
import com.multiplatform.webview.web.NativeWebView
import kotlin.reflect.KFunction0

interface Platform {
    val version: String

    val name: String

    val osUserAgent:String


    @Composable
    fun screenSize(): Size

    @Composable
    fun getNavigationBarHeight(): Dp

    @Composable
    fun getNavigationBarHeight(ignoreInvisible:Boolean): Dp

    @Composable
    fun getTabBarHeight(): Dp


    fun installApk(
        file: FileImpl,
        dialogCallback: KFunction0<Unit>,
        toaster: ToasterState,
        platform: PlatformContext,
        intentLauncher: IntentLauncherProvider
    )

    fun nativeWebviewHandle(webview: NativeWebView)

//    @Composable
//    fun getFontFamily(): FontFamily?

}
public val LocalIntentLauncherProvider: ProvidableCompositionLocal<IntentLauncherProvider> =
    staticCompositionLocalOf {
        error("No intentLauncher provided")
    }

expect class IntentLauncherProvider

expect fun PlatformContext.getAppVersionName(): String
expect fun PlatformContext.getMacAddress(isPrivacyGranted:Boolean): String
expect fun PlatformContext.getManufacturer(isPrivacyGranted:Boolean): String

expect fun encodeUrl(value: String,full:Boolean=true): String


expect fun getPlatform(): Platform



expect class DecimalFormat() {
    fun format(double: Double, decimal: Int): String
    fun format(float: Float, decimal: Int): String
}

@Composable
expect fun isSystemInDarkThemeImpl(): Boolean

expect fun isAndroid(): Boolean

expect fun isIos(): Boolean
expect fun isWeb(): Boolean
expect fun isWindows(): Boolean
expect fun isMac(): Boolean
expect fun isLinux(): Boolean