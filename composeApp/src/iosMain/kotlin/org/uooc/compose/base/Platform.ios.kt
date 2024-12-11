@file:Suppress("unused", "unchecked_cast")

package org.uooc.compose.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil3.PlatformContext
import com.dokar.sonner.ToasterState
import com.github.jing332.filepicker.base.FileImpl
import com.multiplatform.webview.web.NativeWebView
import kotlinx.cinterop.ForeignException
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.OSVersion
import org.jetbrains.skiko.available
import platform.Foundation.NSBundle
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSString
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.create
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.valueForKey
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.statusBarManager
import platform.darwin.NSUInteger
import kotlin.reflect.KFunction0

class IOSPlatform : Platform {
    override val version: String
        get() = UIDevice.currentDevice.systemVersion

    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion


    override val osUserAgent: String
        get() = "iOS"


    override fun installApk(
        file: FileImpl,
        dialogCallback: KFunction0<Unit>,
        toaster: ToasterState,
        platform: PlatformContext,
        intentLauncher: IntentLauncherProvider
    ) {
        //do nothing
    }

    override fun nativeWebviewHandle(webview: NativeWebView) {

    }

    @Composable
    override fun screenSize(): Size {
        return LocalWindowInfo.current.containerSize.toSize()
    }

    private var navigationBarHeight = 0.dp

    @Composable
    override fun getNavigationBarHeight(): Dp {
        return getNavigationBarHeight(ignoreInvisible = true)
    }

    @Composable
    override fun getNavigationBarHeight(ignoreInvisible: Boolean): Dp {
        return if (navigationBarHeight > 0.dp) navigationBarHeight else (with(LocalDensity.current) {
            val fl =
                (UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow)?.safeAreaInsets?.useContents { this.bottom }
                    ?.toFloat() ?: 0f
            fl.dp.let {
                if (ignoreInvisible) {
                    if (it <= 0.dp) {
                        // 导航栏高度,最少保留20dp
                        20.dp
                        0.dp
                    } else it
                } else {
                    // 获取真实的高度
                    it
                }
            }
        }).apply {
            navigationBarHeight = this
        }
    }


    @Composable
    override fun getTabBarHeight(): Dp {
        return if (available(OS.Ios to OSVersion(major = 13))) {
            val window = UIApplication.sharedApplication.windows.first {
                (it as UIWindow).isKeyWindow()
            } as? UIWindow
            window?.windowScene?.statusBarManager?.statusBarFrame?.useContents {
                with(LocalDensity.current) {
                    size.height.toInt().dp
                }
            } ?: 0.dp
        } else {
            UIApplication.sharedApplication.statusBarFrame.useContents {
                with(LocalDensity.current) {
                    size.height.toInt().dp
                }
            }
        }
    }




}

private val _platform: Platform = IOSPlatform()

actual fun getPlatform(): Platform = _platform

@Composable
actual fun isSystemInDarkThemeImpl(): Boolean {
    val window = UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
    val traitCollection = window?.rootViewController?.traitCollection
    return traitCollection?.userInterfaceStyle == UIUserInterfaceStyle.UIUserInterfaceStyleDark
}

actual fun isAndroid() = false
actual fun isIos() = true
actual fun isWeb() = false
actual fun isWindows() = false
actual fun isMac() = false
actual fun isLinux() = false
actual class DecimalFormat actual constructor() {
    actual fun format(float: Float, decimal: Int): String {
        return format(float.toDouble(), decimal)
    }

    actual fun format(double: Double, decimal: Int): String {
        val formatter = NSNumberFormatter()
        formatter.minimumFractionDigits = 0u
        //kotlin Int 转NSUInteger
        formatter.maximumFractionDigits = decimal.convert<NSUInteger>()
        formatter.numberStyle = 1u //Decimal
        return formatter.stringFromNumber(NSNumber(double))!!
    }
}


actual fun PlatformContext.getAppVersionName(): String {
    return try {
        val version =
            (NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String)
                ?: return ""
        version
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}


actual fun PlatformContext.getMacAddress(isPrivacyGranted: Boolean): String {
    return if (isPrivacyGranted) {
        // iOS 不能直接获取 MAC 地址，可能使用 UUID 作为替代
        UIDevice.currentDevice.identifierForVendor?.UUIDString ?: ""
    } else ""
}

actual fun PlatformContext.getManufacturer(isPrivacyGranted: Boolean): String {
    return if (isPrivacyGranted) {
        "${UIDevice.currentDevice.systemName}_${UIDevice.currentDevice.model}_${UIDevice.currentDevice.systemVersion}"
    } else ""
}

// 传值是是否需要完整编码, 比如https://source5.uooconline.com/course335/video/5.5.3.3 IP地址与域名.srt
// 编码结果应该为 https://source5.uooconline.com/course335/video/5.5.3.1%20TCPIP%E5%8D%8F%E8%AE%AE.srt
actual fun encodeUrl(value: String,full:Boolean): String {
    return if(full){
        NSString.create(string = value).stringByAddingPercentEncodingWithAllowedCharacters(
            NSCharacterSet.URLQueryAllowedCharacterSet
        ) ?: value
    }else{
        // 分离协议和路径，只编码路径
        val regex = "(https?://[^/]+)(/.*)".toRegex()
        val matchResult = regex.find(value)
        if (matchResult != null) {
            val (domain, path) = matchResult.destructured
            val p = (NSString.create(string = path).stringByAddingPercentEncodingWithAllowedCharacters(
                NSCharacterSet.URLQueryAllowedCharacterSet
            ) ?: path)
            "$domain/${p}"
        } else {
            // 若URL格式不匹配，直接返回原值
            value
        }
    }
}

//fun ObjCObject.isPointerValid(): Boolean {
//    // 确保 objectPtr 不为空，并且能够响应某些选择器（方法）
//    val nsObject = this.objcPtr()
//    return this.respondsToSelecto(NSSelectorFromString("description"))
//        .apply { println("isPointerValid:$this") }
//}

fun UIView.checkWildPointer(): Boolean {
    return try {
//        if(this is CMPInteropWrappingView)return false
        if (this.respondsToSelector(NSSelectorFromString("description"))) {
            if (this.valueForKey("window") != null) {
//                if(this.isPointerValid().not())return true
                this.window == null
            } else false
        } else {
            false
        }
    } catch (e: ForeignException) {
        false
    }
}

@OptIn(FreezingIsDeprecated::class)
fun UIViewController.checkWildPointer(): Boolean {
    return try {
//        if(this is CMPInteropWrappingView)return false
        if (this.respondsToSelector(NSSelectorFromString("description"))) {
            if (this.valueForKey("view") != null) {
//                if(this.isPointerValid().not())return true
                this.view == null
            } else false
        } else {
            false
        }
    } catch (e: ForeignException) {
        false
    }
}

actual class IntentLauncherProvider
