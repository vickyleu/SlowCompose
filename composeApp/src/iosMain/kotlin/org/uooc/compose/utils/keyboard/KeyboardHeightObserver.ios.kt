package org.uooc.compose.utils.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.PlatformContext
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import kotlinx.coroutines.MainScope
import org.uooc.compose.utils.CommonControllerConfiguration
import org.uooc.compose.utils.LocalComposeUIViewControllerConfiguration
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSValue
import platform.UIKit.CGRectValue
import platform.UIKit.UIKeyboardFrameEndUserInfoKey
import platform.UIKit.UIKeyboardWillChangeFrameNotification
import platform.UIKit.UIKeyboardWillHideNotification
import platform.UIKit.UIScreen
import platform.darwin.NSObject

actual class PlatformKeyboardHeightObserver actual constructor(
    context: PlatformContext,
    density: Density,
    navigationBarHeight: Int,
    onKeyboardChanged: (KeyboardHeightNotification) -> Unit
) : KeyboardHeightObserver by IOSKeyboardHeightObserver(
    navigationBarHeight, density,
    onKeyboardChanged
)


class IOSKeyboardHeightObserver(
    private val navigationBar: Int,
    private val density: Density,
    private val onKeyboardChanged: (KeyboardHeightNotification) -> Unit
) : KeyboardHeightObserver {
    private var previousHeight = 0

    private val objDelegate = object : NSObject() {
        @ObjCAction
        @Suppress("UNUSED")
        fun keyboardWillShow(notification: NSNotification) {
            keyboardWillShowImp(notification)
        }

        @ObjCAction
        @Suppress("UNUSED")
        fun keyboardWillChange(notification: NSNotification) {
            keyboardWillChangeImp(notification)
        }

        @ObjCAction
        @Suppress("UNUSED")
        fun keyboardWillHide(notification: NSNotification) {
            keyboardWillHideImp(notification)
        }

    }
    override var isObserving = false
    private val scope = MainScope()

    override fun startObserving() {
        isObserving = true
        NSNotificationCenter.defaultCenter.addObserver(
            observer = objDelegate,
            selector = NSSelectorFromString("${objDelegate::keyboardWillChange.name}:"),
            name = UIKeyboardWillChangeFrameNotification,
            `object` = null
        )
        NSNotificationCenter.defaultCenter.addObserver(
            observer = objDelegate,
            selector = NSSelectorFromString("${objDelegate::keyboardWillHide.name}:"),
            name = UIKeyboardWillHideNotification,
            `object` = null
        )
    }

    override fun stopObserving() {
        if (isObserving.not()) return
        NSNotificationCenter.defaultCenter.removeObserver(objDelegate)
    }

    fun keyboardWillShowImp(notification: NSNotification) {
        val userInfo = notification.userInfo ?: return
        val aValue = userInfo[UIKeyboardFrameEndUserInfoKey] as? NSValue ?: return
        val keyboardRect = aValue.CGRectValue.useContents { this }
        val height = keyboardRect.size.height.toInt()
        println("keyboardWillShowImp:$height")
        imeMeasure(height)
    }

    @OptIn(InternalComposeApi::class)
    fun keyboardWillChangeImp(notification: NSNotification) {
        val userInfo = notification.userInfo ?: return
        val aValue = userInfo[UIKeyboardFrameEndUserInfoKey] as? NSValue ?: return
        val keyboardRect = aValue.CGRectValue
        // Convert the keyboard frame to the window's coordinate system
        val scale = UIScreen.mainScreen.scale
        // 将高度从 points 转换为 pixels
        val keyboardHeight = (keyboardRect.useContents { this.size.height } * scale).toInt()
        imeMeasure(keyboardHeight)
    }

    @OptIn(InternalComposeUiApi::class)
    private fun imeMeasure(height: Int) {
        if (height == 0) {
            onKeyboardHeightChanged(KeyboardHeightNotification(0.dp))
        } else {
            onKeyboardHeightChanged(KeyboardHeightNotification(with(density) { height.toDp() }))
        }

    }

    fun keyboardWillHideImp(notification: NSNotification) {
        onKeyboardHeightChanged(KeyboardHeightNotification(0.dp))
    }

    override fun onKeyboardHeightChanged(notification: KeyboardHeightNotification) {
        onKeyboardChanged.invoke(notification)
    }
}

@Composable
actual fun KeyboardHeightNotification.detectXiaomi(
    dp: Dp,
    density: Density
): Dp = dp

actual fun CommonControllerConfiguration.switchIosKeyboardBehavior(keyboardAppearsWhenFocus: Boolean) {
    this.onFocusBehavior = if (keyboardAppearsWhenFocus) {
        OnFocusBehavior.FocusableAboveKeyboard
    } else {
        OnFocusBehavior.DoNothing
    }
    this.delegate.prefersStatusBarHidden
}