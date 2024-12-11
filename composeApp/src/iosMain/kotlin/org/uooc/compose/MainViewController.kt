package org.uooc.compose

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.LocalPlatformContext
import com.dokar.sonner.rememberToasterState
import com.huhx.picker.util.LocalStoragePermission
import com.huhx.picker.util.StoragePermissionUtil
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.toKString
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.uooc.compose.utils.KUIEdgeInsetsZero
import org.uooc.compose.utils.LocalStorageLauncher
import org.uooc.compose.utils.StorageLauncher
import org.uooc.compose.utils.pref
import platform.Foundation.NSClassFromString
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsets
import platform.UIKit.UIEdgeInsetsZero
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.additionalSafeAreaInsets
import platform.objc.class_addMethod
import platform.objc.class_getInstanceMethod
import platform.objc.class_getName
import platform.objc.imp_implementationWithBlock
import platform.objc.method_getTypeEncoding
import platform.objc.objc_allocateClassPair
import platform.objc.objc_getClass
import platform.objc.objc_registerClassPair
import platform.objc.object_setClass


@Suppress("FunctionName", "unused")
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
fun MainViewController(delegateImpl: org.uooc.compose.base.AppDelegate): UIViewController {
    val composeContainerObserver = ComposeContainerObserver()

    var restoreConfig = ComposeUIViewControllerConfiguration()

    val controller = ComposeUIViewController(configure = {
        this.delegate = delegateImpl
        // 键盘弹出时，不自动调整视图的大小
        this.onFocusBehavior = OnFocusBehavior.DoNothing
        this.opaque = true
        this.platformLayers = true
        restoreConfig = this
    }) {


        val lifecycle = LocalLifecycleOwner.current
        val platformContext = LocalPlatformContext.current
        val impl = StoragePermissionUtil(platformContext, lifecycle.lifecycle, MainScope())
        val launcher = StorageLauncher()

        val toaster = rememberToasterState()
        CompositionLocalProvider(LocalAppDelegate provides delegateImpl) {
            CompositionLocalProvider(LocalStoragePermission provides impl) {
                CompositionLocalProvider(LocalStorageLauncher provides launcher) {
                    App(toaster, restoreConfig, )
                }
            }
        }
        /*val controller = LocalUIViewController.current
        LaunchedEffect(Unit){
            controller.view.translatesAutoresizingMaskIntoConstraints = false
            // 设置约束，使视图覆盖整个屏幕，包括安全区域
            NSLayoutConstraint.activateConstraints(listOf(
                controller.view.topAnchor.constraintEqualToAnchor(controller.view.topAnchor),
                controller.view.bottomAnchor.constraintEqualToAnchor(controller.view.bottomAnchor),
                controller.view.leadingAnchor.constraintEqualToAnchor(controller.view.leadingAnchor),
                controller.view.trailingAnchor.constraintEqualToAnchor(controller.view.trailingAnchor),
            ))
        }*/
//    App(isLoading = false, loadingProgress = 1f)
    }.apply {
        disableSafeArea()
        this.overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleDark
        this.automaticallyAdjustsScrollViewInsets = false
        this.additionalSafeAreaInsets = KUIEdgeInsetsZero
        this.view.backgroundColor = UIColor.whiteColor
    }
    return controller
}


@OptIn(BetaInteropApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)
fun UIViewController.disableSafeArea() {
    val className = "${this.view.`class`() ?: return}"
    val viewClass = objc_getClass(className) ?: return
    val name = class_getName(viewClass as ObjCClass)!!.toKString()
    val viewSubclassName = name + "_IgnoreSafeArea"
    val viewSubclass = NSClassFromString(viewSubclassName)
    if (viewSubclass != null) {
        object_setClass(this.view, viewSubclass)
    } else {
        val viewSubclassAlloc = objc_allocateClassPair(viewClass, viewSubclassName, 0u) ?: return
        val method =
            class_getInstanceMethod(UIView.`class`(), NSSelectorFromString("safeAreaInsets"))
        if (method != null) {
            val safeAreaInsets: (Any) -> UIEdgeInsets = abc@{
                return@abc UIEdgeInsetsZero
            }
            class_addMethod(
                viewSubclassAlloc, NSSelectorFromString("safeAreaInsets"),
                imp_implementationWithBlock(safeAreaInsets),
                method_getTypeEncoding(method)?.toKString()
            )
        }
        objc_registerClassPair(viewSubclassAlloc)
        object_setClass(view, viewSubclassAlloc)
    }
}


