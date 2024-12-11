package org.uooc.compose.base

import androidx.compose.ui.uikit.ComposeUIViewControllerDelegate
import org.uooc.compose.utils.pref
import platform.UIKit.UIApplicationDelegateProtocol

actual interface AppDelegate : ComposeUIViewControllerDelegate/*,UIApplicationDelegateProtocol*/ {

    /*override fun viewDidAppear(animated: Boolean) {
    }

    override fun viewDidLoad() {
    }

    override fun viewWillAppear(animated: Boolean) {
    }

    override fun viewDidDisappear(animated: Boolean) {
    }

    override fun viewWillDisappear(animated: Boolean) {
    }*/
    actual fun initPrivacyUtil()

    //将以上ios 生命周期方法转换成以下Android生命周期的调用
    actual fun onCreate()
    actual fun onDestroy()
    actual fun onStart()
    actual fun onStop()
    actual fun onResume()
    actual fun onPause()
    actual fun onRestart()
    actual fun onLowMemory()
    actual fun onTrimMemory(level: Int)
    actual fun onWindowFocusChanged(hasFocus: Boolean)

    actual fun onKillProcess()

    override fun viewDidLoad() {
        onCreate()
    }

    override fun viewWillAppear(animated: Boolean) {
        onStart()
    }

    override fun viewDidAppear(animated: Boolean) {
        onResume()
    }

    override fun viewWillDisappear(animated: Boolean) {
        onPause()
    }

    override fun viewDidDisappear(animated: Boolean) {
        onStop()
    }
    // 并且需要实现onDestroy调用,将iOS 的deinit转换成Android的onDestroy,目前是kmm项目
    // kmm的kotlin native 中没有deinit方法

}
