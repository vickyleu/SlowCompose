package org.uooc.compose.utils


import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.github.jing332.filepicker.base.FileImpl
import com.github.jing332.filepicker.base.uri
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import observer.ObserverProtocol
import org.uooc.compose.base.checkWildPointer
import org.uooc.compose.core.uoocDispatchers
import platform.CoreGraphics.CGFloat
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGPointZero
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.CoreGraphics.CGSizeZero
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.NSNumber
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSValue
import platform.Foundation.addObserver
import platform.Foundation.create
import platform.Foundation.numberWithBool
import platform.Foundation.numberWithDouble
import platform.Foundation.numberWithInteger
import platform.Foundation.pathComponents
import platform.Foundation.removeObserver
import platform.UIKit.CGRectValue
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsets
import platform.UIKit.UIEdgeInsetsZero
import platform.UIKit.UIResponder
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.NSInteger
import platform.darwin.NSObject
import platform.darwin.NSUInteger


fun String.toNSString(): NSString {
    return NSString.create(string = this)
}


fun NSRect(any: Any?): CGRect? {
    return (any as? NSValue)?.CGRectValue?.useContents { this }
}


fun Int.toNSInteger(): NSInteger {
    return this.toLong().convert()
}

fun NSInteger.toNSNumber(): NSNumber {
    return NSNumber.numberWithInteger(this)
}

fun CGFloat.toNSNumber(): NSNumber {
    return NSNumber.numberWithDouble(this)
}


fun Boolean.toNSNumber(): NSNumber {
    return NSNumber.numberWithBool(this)
}


val Float.cgFloat: CGFloat
    get() = this.toDouble()

fun getCacheDirectory(name:String): NSURL {
    val cacheDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSCachesDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val url = cacheDirectory?.URLByAppendingPathComponent(name)!!
    val file = FileImpl(url.path!!)
    file.mkdirs()
    return NSURL(string = url.path!!)
}

fun isLowMemoryDevice(): Boolean {
    // 获取设备的物理内存
    val physicalMemory = NSProcessInfo.processInfo.physicalMemory
    // 判断物理内存是否小于某个阈值，比如1GB（1073741824字节）
    return physicalMemory < 1073741824.toULong() // 1GB
}

fun CValue<CGRect>.toKRect(): Rect {
    return useContents {
        Rect(
            Offset(this.origin.x.toFloat(), this.origin.y.toFloat()),
            Size(width = this.size.width.toFloat(), height = this.size.height.toFloat())
        )
    }
}

@Suppress("DEPRECATION")
val CGRect.Companion.zero: CValue<CGRect>
    get() = CGRectZero.readValue()

@Suppress("DEPRECATION")
val UIEdgeInsets.Companion.zero: CValue<UIEdgeInsets>
    get() = UIEdgeInsetsZero.readValue()

@Suppress("DEPRECATION")
val CGPoint.Companion.zero: CValue<CGPoint>
    get() = CGPointZero.readValue()

@Suppress("DEPRECATION")
val CGSize.Companion.zero: CValue<CGSize>
    get() = CGSizeZero.readValue()

@Suppress("DEPRECATION")
val KCGPointZero = CGPoint.zero

@Suppress("DEPRECATION")
val KUIEdgeInsetsZero = UIEdgeInsets.zero

@Suppress("DEPRECATION")
val KCGRectZero = CGRect.zero

@Suppress("DEPRECATION")
val KCGSizeZero = CGSize.zero

fun Int.toNSInt(): NSInteger {
    return this.convert<NSInteger>()
}

fun Int.toNSUInt(): NSUInteger {
    return this.toUInt().toNSUInt()
}

fun UInt.toNSUInt(): NSUInteger {
    return this.convert<NSUInteger>()
}


fun UIView.parent(count: Int): UIView? {
    if (count <= 0) return null
    var c = count - 1
    var v: UIView? = this.superview?.let {
        if (it.checkWildPointer().not()) {
            it
        }else null
    }
    while ((c-- > 0 && v != null)) {
        v = v.superview?.let {
            if (it.checkWildPointer().not()) {
                it
            }else null
        }
    }
    return v
}

/**
 * UIKitView factory maybe reattach to window,Observe backgroundColor change
 */
class InteropWrappingViewWatching(val view: UIView) : NSObject(), ObserverProtocol {
    override fun observeValueForKeyPath(
        keyPath: String?,
        ofObject: Any?,
        change: Map<Any?, *>?,
        context: COpaquePointer?
    ) {
        if (keyPath == "backgroundColor") {
            if (view.backgroundColor != UIColor.clearColor) {
                view.opaque = true
                view.backgroundColor = UIColor.clearColor
                view.setTag(10088)
                view.removeObserver(this, "backgroundColor")
            }
        }
    }
}

fun UIView.removeInteropWrappingViewColor(scope: CoroutineScope) {
    if (tag.toInt() != 10087 && tag.toInt() != 10088) {
        opaque = true
        backgroundColor = UIColor.clearColor
        val parent = superview ?: return
        if(parent.checkWildPointer())return
        memScoped {
            val obs = InteropWrappingViewWatching(parent)
            findViewController()?.apply controller@{
                scope.launch {
                    var notStop = true
                    while (notStop) {
                        withContext(uoocDispatchers.io) {
                            delay(100)
                            withContext(uoocDispatchers.main) {
                                if (this@controller.isViewLoaded()) {
                                    notStop = false
                                    if(parent.checkWildPointer())return@withContext
                                    parent.apply {
                                        opaque = true
//                                        setBackgroundColor(UIColor.clearColor)
//                                        backgroundColor = UIColor.clearColor
                                        setTag(10087)
                                        addObserver(
                                            obs,
                                            forKeyPath = "backgroundColor",
                                            options = NSKeyValueObservingOptionNew,
                                            context = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun UIView.removeControllerColor(scope: CoroutineScope) {
    if (tag.toInt() != 10086) {
        opaque = true
        backgroundColor = UIColor.clearColor
        findViewController()?.apply controller@{
            if(this.checkWildPointer())return
            scope.launch {
                var notStop = true
                while (notStop) {
                    withContext(uoocDispatchers.io) {
                        delay(100)
                        withContext(uoocDispatchers.main) {
                            if(this@controller.checkWildPointer())return@withContext
                            if (this@controller.isViewLoaded()) {
                                notStop = false
                                this@controller.view.opaque = true
                                this@controller.view.backgroundColor = UIColor.whiteColor
                                this@controller.view.setTag(10086)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun UIView.findViewController(): UIViewController? {
    var nextResponder: UIResponder? = this
    while (nextResponder != null) {
        if (nextResponder is UIViewController) {
            return nextResponder
        }
        nextResponder = nextResponder.nextResponder
    }
    return null
}


val CValue<CGPoint>.x
    get() = this.toKOffset().x

val CValue<CGPoint>.y
    get() = this.toKOffset().y

operator fun CValue<CGPoint>.plusAssign(point: CValue<CGPoint>) {
    val p = point.toKOffset()
    this.useContents {
        this.x += p.x
        this.y += p.y
    }
}

operator fun CValue<CGPoint>.div(scalar: CGFloat): CValue<CGPoint> {
    return this.useContents {
        CGPointMake(x = this.x / scalar, y = this.y / scalar)
    }
}

operator fun CValue<CGPoint>.timesAssign(factor: CGFloat) {
    useContents {
        this.x *= factor
        this.y *= factor
    }
}


// 扩展函数定义来计算向量的大小并进行比较
operator fun CValue<CGPoint>.compareTo(factor: CGFloat): Int {
    return this.useContents {
        val magnitude = kotlin.math.sqrt(this.x * this.x + this.y * this.y)
        when {
            magnitude < factor -> -1
            magnitude > factor -> 1
            else -> 0
        }
    }
}


fun CValue<CGPoint>.toKOffset(): Offset {
    return useContents {
        Offset(this.x.toFloat(), this.y.toFloat())
    }
}

fun CGRect.toKRect(): Rect {
    return Rect(
        Offset(this.origin.x.toFloat(), this.origin.y.toFloat()),
        Size(width = this.size.width.toFloat(), height = this.size.height.toFloat())
    )
}

fun Size.toCGSize(): CValue<CGSize> {
    return CGSizeMake(this.width.cgFloat, this.height.cgFloat)
}

fun CValue<CGSize>.toKSize(): Size {
    return useContents {
        Size(width = this.width.toFloat(), height = this.height.toFloat())
    }
}


fun CGSize.toKSize(): Size {
    return Size(width = this.width.toFloat(), height = this.height.toFloat())
}

fun Rect.toCGRect(): CValue<CGRect> {
    return CGRectMake(
        this.topLeft.x.cgFloat,
        this.topLeft.y.cgFloat,
        this.topLeft.x.cgFloat+this.size.width.cgFloat,
        this.topLeft.y.cgFloat+this.size.height.cgFloat
    )
}