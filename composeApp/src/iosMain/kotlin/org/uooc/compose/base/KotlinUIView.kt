package org.uooc.compose.base

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import org.uooc.compose.utils.KCGRectZero
import platform.CoreGraphics.CGRect
import platform.Foundation.NSCoder
import platform.UIKit.UIView

@OptIn(BetaInteropApi::class)
class KotlinUIView : UIView {
    constructor() : this(frame = KCGRectZero)

    @OverrideInit
    constructor(frame: CValue<CGRect>) : super(frame)

    @OverrideInit
    constructor(coder: NSCoder) : super(coder)
}