package org.uooc.compose.utils.keyboard

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.PlatformContext
import org.uooc.compose.utils.CommonControllerConfiguration


expect fun CommonControllerConfiguration.switchIosKeyboardBehavior(keyboardAppearsWhenFocus: Boolean)

interface KeyboardHeightObserver {
    fun onKeyboardHeightChanged(notification: KeyboardHeightNotification)
    fun startObserving()
    fun stopObserving()
    val isObserving: Boolean
}


data class KeyboardHeightNotification(private val dp: Dp) {
    val dpValue = mutableStateOf(dp)

    // 输入法必须对应到当前最后一个window,否则是拿不到真实的高度的,Notification 是系统发出的通知, 但是高度并不正确,需要在window中获取真实高度,所以这里的consume必须是在最后一个window里面获取
    // 比如弹出的dialog
    @Composable
    fun consume() {// TODO 🤡 🤡 🤡 🤡 dirty hack to consume the ime height for ios calculation, fucking jetbrains idiots
        if (dp > 0.dp) {
            with(LocalDensity.current) {
                // Consume the notification
                val ime = WindowInsets.ime
                val top = ime.getTop(this)
                val bottom = ime.getBottom(this)
                val v = (top + bottom).toDp()
                if (v == 0.dp) return
                dpValue.value = v.let {
                    detectXiaomi(it, density = this)
                }
                return
            }
        }
    }
}

@Composable
expect fun KeyboardHeightNotification.detectXiaomi(dp: Dp, density: Density): Dp


expect class PlatformKeyboardHeightObserver(
    context: PlatformContext,
    density: Density,
    navigationBarHeight: Int,
    onKeyboardChanged: (KeyboardHeightNotification) -> Unit
) : KeyboardHeightObserver {
    override fun onKeyboardHeightChanged(notification: KeyboardHeightNotification)
    override fun startObserving()
    override fun stopObserving()
    override val isObserving: Boolean
}