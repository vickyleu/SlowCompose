package chaintech.videoplayer.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.MutableState
import androidx.compose.ui.uikit.InterfaceOrientation
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMedia.CMTime
import platform.Foundation.NSString
import platform.Foundation.setValue
import platform.Foundation.stringWithFormat
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIInterfaceOrientationLandscapeLeft
import platform.UIKit.UIInterfaceOrientationMask
import platform.UIKit.UIInterfaceOrientationMaskLandscape
import platform.UIKit.UIInterfaceOrientationMaskLandscapeLeft
import platform.UIKit.UIInterfaceOrientationMaskLandscapeRight
import platform.UIKit.UIInterfaceOrientationMaskPortrait
import platform.UIKit.UIInterfaceOrientationPortrait
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.UIKit.UIWindowSceneGeometryPreferences
import platform.UIKit.UIWindowSceneGeometryPreferencesIOS
import platform.UIKit.childViewControllers
import platform.UIKit.setStatusBarHidden

actual fun formatMinSec(value: Int): String {
    val toSecond = (value.toLong()/1000).toInt()
    val hour = (toSecond / 3600)
    val remainingSecondsAfterHours = (toSecond % 3600)
    val minutes = remainingSecondsAfterHours / 60
    val seconds = remainingSecondsAfterHours % 60

    val strHour : String = if (hour > 0) { NSString.stringWithFormat(format = "%02d:", hour)
    } else { "" }
    val strMinutes : String = NSString.stringWithFormat(format = "%02d:", minutes)
    val strSeconds : String = NSString.stringWithFormat(format = "%02d", seconds)

    return "${strHour}${strMinutes}${strSeconds}"
}

@OptIn(ExperimentalForeignApi::class)
@Suppress("unused","FunctionName")
internal fun CMTimeGetMilliseconds(time: CValue<CMTime>): Double {
    time.useContents {
        return (value.toDouble() * 1000.0) / timescale.toDouble()
    }
}

actual fun formatInterval(value: Int): Int {
    return value * 1000
}

@OptIn(InternalComposeApi::class)
@Composable
actual fun LandscapeOrientation(
    isLandscape: MutableState<Boolean>,
    content: @Composable () -> Unit
) {
    DisposableEffect(isLandscape.value) {
        val window = UIApplication.sharedApplication.keyWindow
        val windowScene = window?.windowScene
        windowScene?.apply {
            requestGeometryUpdateWithPreferences(UIWindowSceneGeometryPreferencesIOS().apply {
                if (isLandscape.value) {
                    UIApplication.sharedApplication.setStatusBarHidden(hidden = true,true)
                    interfaceOrientations =  UIInterfaceOrientationMaskLandscapeRight
                } else {
                    UIApplication.sharedApplication.setStatusBarHidden(hidden = false,true)
                    interfaceOrientations = UIInterfaceOrientationMaskPortrait
                }
            }, errorHandler = null)
        }
        onDispose {
            windowScene?.requestGeometryUpdateWithPreferences(UIWindowSceneGeometryPreferencesIOS().apply {
                UIApplication.sharedApplication.setStatusBarHidden(hidden = false,true)
                interfaceOrientations = UIInterfaceOrientationMaskPortrait//不存在的字段
            }, errorHandler = null)
        }
    }
    content()
}


