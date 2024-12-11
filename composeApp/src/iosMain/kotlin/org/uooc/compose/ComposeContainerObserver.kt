package org.uooc.compose

import kotlinx.cinterop.COpaquePointer
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.darwin.NSObject

class ComposeContainerObserver : NSObject(), observer.ObserverProtocol {
    override fun observeValueForKeyPath(
        keyPath: String?,
        ofObject: Any?,
        change: Map<Any?, *>?,
        context: COpaquePointer?
    ) {
        if (keyPath == "backgroundColor") {
            val uiView = ofObject as? UIView ?: return
            val newColor = uiView.backgroundColor
            if (newColor != UIColor.whiteColor && newColor != UIColor.clearColor) {
                uiView.backgroundColor = UIColor.whiteColor
            }
        }
    }

    fun addObserver(uiView: UIView) {
        uiView.addObserver(
            this,
            forKeyPath = "backgroundColor",
            options = NSKeyValueObservingOptionNew,
            context = null
        )
    }

    fun removeObserver(uiView: UIView) {
        uiView.removeObserver(this, forKeyPath = "backgroundColor")
    }
}