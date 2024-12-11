@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package org.uooc.compose.base

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.fastMapIndexedNotNull
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import cocoapods.DTFoundation.DTAnimatedGIFFromData
import what.the.fuck.with.iosMath.cinterop.MTFontManager
import what.the.fuck.with.iosMath.cinterop.MTMathUILabel
import what.the.fuck.with.iosMath.cinterop.MTMathUILabelMode
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.select.Elements
import com.multiplatform.webview.util.toUIColor
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.convert
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.uooc.compose.base.taghandler.ATagHandler
import org.uooc.compose.base.taghandler.Base64ImageHandler
import org.uooc.compose.base.taghandler.DTHrefAttachment
import org.uooc.compose.base.taghandler.DTTableTextAttachment
import org.uooc.compose.base.taghandler.EMTagHandler
import org.uooc.compose.base.taghandler.MathTextAttachment
import org.uooc.compose.base.taghandler.SpanTagHandler
import org.uooc.compose.base.taghandler.TableHandler
import org.uooc.compose.utils.parent
import platform.CoreGraphics.CGContextRef
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeEqualToSize
import platform.CoreGraphics.CGSizeMake
import platform.CoreGraphics.CGSizeZero
import platform.Foundation.NSAttributedString
import platform.Foundation.NSCoder
import platform.Foundation.NSData
import platform.Foundation.NSMakeRange
import platform.Foundation.NSNumber
import platform.Foundation.NSPredicate
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.containsString
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.length
import platform.UIKit.UIColor
import platform.UIKit.UIFontDescriptor
import platform.UIKit.UIFontTextStyleBody
import platform.UIKit.UIImage
import platform.UIKit.UILabel
import platform.UIKit.UIScreen
import platform.UIKit.UITapGestureRecognizer
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewContentMode
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSEC_PER_SEC
import platform.darwin.NSObject
import platform.darwin.dispatch_after
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import uooc.DTCoreText.CGFLOAT_HEIGHT_UNKNOWN
import uooc.DTCoreText.DTAttributedLabel
import uooc.DTCoreText.DTAttributedTextContentView
import uooc.DTCoreText.DTAttributedTextContentViewDelegateProtocol
import uooc.DTCoreText.DTCoreTextLayoutFrame
import uooc.DTCoreText.DTCoreTextLayouter
import uooc.DTCoreText.DTDefaultFontDescriptor
import uooc.DTCoreText.DTDefaultFontSize
import uooc.DTCoreText.DTHTMLAttributedStringBuilder
import uooc.DTCoreText.DTHTMLAttributedStringBuilderWillFlushCallback
import uooc.DTCoreText.DTImageTextAttachment
import uooc.DTCoreText.DTLazyImageView
import uooc.DTCoreText.DTLazyImageViewDelegateProtocol
import uooc.DTCoreText.DTTextAttachment
import uooc.DTCoreText.DTWillFlushBlockCallBack
import uooc.DTCoreText.NSBaseURLDocumentOption
import uooc.DTCoreText.TagRandererView
import kotlin.collections.set
import kotlin.experimental.ExperimentalNativeApi
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ref.WeakReference

@OptIn(
    BetaInteropApi::class, ExperimentalNativeApi::class, ExperimentalForeignApi::class,
    ExperimentalForeignApi::class, ExperimentalObjCName::class
)
@Composable
actual fun RichTextPlatformView(
    state: RichTextCompose,
    style: TextStyle,
    modifier: Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color
) {
    val images = remember { mutableListOf<String>() }
//    var width by remember(key1 = state) { state.width }
//    var height by remember(key1 = state) { state.height }

    val scale = UIScreen.mainScreen.scale.toFloat()
    with(LocalDensity.current) {
        val screenWidth =
            UIScreen.mainScreen.bounds.useContents { this.size.width.toFloat() }.toDp()
        Column(
            modifier.let {
                if (state.isFillWidth) {
                    it.fillMaxWidth()
                } else {
                    it
                }
            },
        ) {
            BoxWithConstraints(
                Modifier.let {
                    if (state.isFillWidth) {
                        it.fillMaxWidth().wrapContentHeight()
                    } else {
                        it.wrapContentSize()
                    }
                }
            ) {
                val www = remember { state.width }
                val hhh = remember { state.height }


                var attrString by remember {
                    val nsString = state.content
                    mutableStateOf(nsString)
                }
                LaunchedEffect(Unit) {
                    snapshotFlow { state.content }.distinctUntilChanged().collect {
                        val nsString = it
                        attrString = nsString
                    }
                }




                var parentView by remember { mutableStateOf<UIView?>(null) }
                val attributedLabel = remember { mutableStateOf<DTAttributedLabel?>(null) }
                var isResize by remember(attrString) { mutableStateOf(false) }
                LaunchedEffect(state, maxWidth.value) {
                    combine(snapshotFlow { www.value }
                        .filter {
                            it.isSpecified && it.value.isNaN().not()
                                    && maxWidth.toPx().toDouble().isNaN().not()
                        }
                        .filter { it.value >= 0f }
                        .distinctUntilChanged(),
                        snapshotFlow { hhh.value }
                            .filter {
                                it.isSpecified && it.value.isNaN().not()
                                        && maxWidth.toPx().toDouble().isNaN().not()
                            }
                            .filter { it.value >= 0f }
                            .distinctUntilChanged()) { w, h ->
                        Pair(w, h)
                    }
                        .filter {
                            /*parentView != null &&*/ attributedLabel != null
                        }
                        .collect {
                            val al = attributedLabel.value ?: return@collect
                            val parent = parentView

                            withContext(Dispatchers.Main) {
                                val w = maxWidth.value.toDouble()
//                                it.first.value.toDouble().coerceIn(
//                                minimumValue = 10.0,
//                                maximumValue =  maxWidth.value.toDouble()
//                            )
                                val h = it.second.value.toDouble()
                                    .coerceAtLeast(10.0)
                                al.setNeedsUpdateConstraints()  // 标记需要更新约束
                                al.setNeedsLayout()  // 标记需要重新布局

                                if(parent!=null){
                                    val parentRect = CGRectMake(
                                        x = parent.frame.useContents { this.origin.x },
                                        y = parent.frame.useContents { this.origin.y },
                                        width = w,
                                        height = h
                                    )
                                    parent.setFrame(parentRect)
                                    parent.setNeedsUpdateConstraints()
                                    parent.setNeedsLayout()
                                }


                                val alRect =  CGRectMake(
                                    x = al.frame.useContents { this.origin.x },
                                    y = al.frame.useContents { this.origin.y },
                                    width = w,
                                    height = h
                                )
                                al.setFrame(alRect)  // 立即应用布局更新

                                println(
                                    "::::::: layoutFrameWithRect ==>widthwwww:$w h=${h}  text:::${
                                        Ksoup.parse(state.content).text().let {
                                            if (it.length > 10) {
                                                it.substring(0, 10)
                                            } else {
                                                it
                                            }
                                        }
                                    }:::"
                                )
                                parent?.layoutIfNeeded()  // 立即应用布局更新
                                al.layoutIfNeeded()  // 立即应用布局更新

                                state.needMeasure = true
                                al.layouter = null
                                al.relayoutText()
                                /*dispatch_async(dispatch_get_main_queue()) {

                                    println(": topParent.setFrame(topParentRect)==>>  layoutFrameWithRect size:w:$w h=${h}")
                                    isResize = true
                                    dispatch_async(dispatch_get_main_queue()) {

                                    }

                                }*/
                            }

                        }
                }
                val coroutineScope = rememberCoroutineScope()
                val htmlString = remember(attrString) {
                    val html = Ksoup.parse(attrString).outerHtml()
                    val htmlFull = Ksoup.parse(html)
                    htmlFull.head().apply {
                        this.appendElement("meta").apply {
                            this.attr("name", "viewport")
                            this.attr("content", "width=device-width, initial-scale=1.0")
                        }
                        this.appendElement("style").apply {
                            this.attr("type", "text/css")
                            this.appendText(
                                """
                                html{
                                    font-size:${style.fontSize.value}px;
                                    width:100%;
                                    height:100%;
                                    margin:0;
                                    padding:0;
                                    text-align:left;
                                }
                                body{
                                    font-size:${style.fontSize.value}px;
                                    width:100%;
                                    height:100%;
                                    margin:0;
                                    padding:0;
                                    text-align:left;
                                }
                                img{
                                    display:block;
                                    max-width:100%;
                                    height:auto;
                                }
                            """.trimIndent()
                            )
                        }
                    }
                    htmlFull.body().apply {
                        val spans: Elements = this.select("span.math-tex")
                        spans.forEach {
                            val formated = it.text()
                                .replace("\\\\(", "\\(")
                                .replace("\\\\)", "\\)")
                                .replace("\\\\", "\\")
                            if (formated.isNotEmpty()) {
                                it.textNodes().forEach {
                                    it.remove()
                                }
                                it.text(formated + "\n")
                            }
                        }
                    }

                    println("attrString:::${attrString}")
                    Ksoup.parse(htmlFull.outerHtml()).outerHtml().let {
                        NSString.create(string = it)
                    }
                }

                val kvo = remember(attrString) {
                    kvoScope(
                        state,
                        images,
                        coroutineScope,
                        hhh,
                        attributedLabel,
                        isResize,
                        www
                    )
                }

                androidx.compose.ui.viewinterop.UIKitView(factory = {
                    val maxRect = Rect(0.0f, 0.0f, maxWidth.value, CGFLOAT_HEIGHT_UNKNOWN)
                    val att =
                        htmlString.getAttributedStringWithHtml(maxRect.width, this@with, style)


                    if (state.needMeasure.not()) {
                        val baseView = UIView(
                            frame = CGRectMake(
                                x = 0.0,
                                y = 0.0,
                                width = www.value.value.toDouble(),
                                height = hhh.value.value.toDouble()
                            )
                        )
                        val label = DTAttributedLabel(
                            frame = CGRectMake(
                                x = 0.0,
                                y = 0.0,
                                width = www.value.value.toDouble(),
                                height = hhh.value.value.toDouble()
                            )
                        ).apply {
                            layoutFrameHeightIsConstrainedByBounds = false
                            relayoutMask =
                                (DTAttributedTextContentViewRelayout.RelayoutOnHeightChanged or
                                        DTAttributedTextContentViewRelayout.RelayoutOnWidthChanged).convert()
                        }
                        baseView.backgroundColor = UIColor.clearColor
                        label.backgroundColor = UIColor.clearColor
                        label.setDelegate(kvo)

                        parentView = baseView
                        baseView.addSubview(label)
                        label.apply {
                            this.layouter = layouter
                            attributedLabel.value = this
                            this.attributedString = att
                        }
                        label.autoresizingMask =
                            UIViewAutoresizingFlexibleHeight or UIViewAutoresizingFlexibleWidth
                        label.tag = state.content.hashCode().toLong()
                        isResize = true
                        baseView
                    } else {
                        println("::::::: layoutFrameWithRect ==>UIKitView text:::${Ksoup.parse(state.content).text().let {
                            if (it.length > 10) {
                                it.substring(0, 10)
                            } else {
                                it
                            }
                        }}:::")

                        val layouter = DTCoreTextLayouter(attributedString = att)
                        val r = layouter.layoutFrameWithRect(
                            CGRectMake(
                                x = 0.0,
                                y = 0.0,
                                width = maxRect.width.toDouble(),
                                height = maxRect.height.toDouble()
                            ),
                            range = NSMakeRange(0u, att.length)
                        )
                        val size = r?.intrinsicContentFrame() ?: CGRectMake(0.0, 0.0, 0.0, 0.0)
                        val nWidth = if (state.isFillWidth) maxWidth else min(
                            size.useContents { this.size.width }.dp,
                            maxWidth
                        ).coerceAtLeast(10.dp)
                        val nHeight =
                            max(size.useContents { this.size.height }.dp, 10.dp).coerceAtLeast(
                                10.dp
                            )

                        val baseView = UIView(
                            frame = CGRectMake(
                                x = 0.0,
                                y = 0.0,
                                width = maxWidth.value.toDouble(),
                                height = nHeight.value.toDouble()
                            )
                        )
                        val label = DTAttributedLabel(
                            frame = CGRectMake(
                                x = 0.0,
                                y = 0.0,
                                width = maxWidth.value.toDouble(),
                                height = nHeight.value.toDouble()
                            )
                        ).apply {
                            layoutFrameHeightIsConstrainedByBounds = false
                            relayoutMask =
                                (DTAttributedTextContentViewRelayout.RelayoutOnHeightChanged or
                                        DTAttributedTextContentViewRelayout.RelayoutOnWidthChanged).convert()
                        }
                        baseView.backgroundColor = UIColor.clearColor
                        label.backgroundColor = UIColor.clearColor
                        label.setDelegate(kvo)
                        parentView = baseView
                        baseView.addSubview(label)
                        label.apply {
                            this.layouter = layouter
                            attributedLabel.value = this
                            this.attributedString = att
                        }
                        label.autoresizingMask =
                            UIViewAutoresizingFlexibleHeight or UIViewAutoresizingFlexibleWidth
                        label.tag = state.content.hashCode().toLong()
                        isResize = true
                        dispatch_after(
                            dispatch_time(
                                `when` = DISPATCH_TIME_NOW,
                                delta = (0.5f * NSEC_PER_SEC.toLong()).toULong().convert()
                            ), dispatch_get_main_queue()
                        ) {
                            if (nWidth > 0.dp && nHeight > 0.dp) {
                                www.value = nWidth
                                hhh.value = nHeight
                                println(
                                    "::::::: layoutFrameWithRect ==>NSEC_PER_SECwidth:${nWidth.value} h=${nHeight.value} ${hhh.value.value} text:::${
                                        Ksoup.parse(state.content).text().let {
                                            if (it.length > 10) {
                                                it.substring(0, 10)
                                            } else {
                                                it
                                            }
                                        }
                                    }:::"
                                )
                            }
                            println("""
                                ::::::: layoutFrameWithRect ==>dispatch_after  hhh.value.value:${ hhh.value.value } text:::${Ksoup.parse(state.content).text().let {
                                if (it.length > 10) {
                                    it.substring(0, 10)
                                } else {
                                    it
                                }
                            }}:::
                            """.trimIndent())
                            state.needMeasure = true
                            attributedLabel.value?.layouter = null
                            attributedLabel.value?.relayoutText()
                        }
                        baseView
                    }

                },
                    modifier = Modifier

                        .let {
                            if (state.isFillWidth) {
                                it.fillMaxWidth()
                                    .wrapContentHeight()
                            } else {
                                it.wrapContentWidth()
                                    .wrapContentHeight()
                                    .requiredWidthIn(
                                        min = max(10.dp, www.value),
                                        max = max(maxWidth, www.value)
                                    )
                            }
                        }
                        .requiredHeightIn(
                            min = max(10.dp, hhh.value),
                            max = max(maxHeight, hhh.value)
                        ),
                    update = { view ->
//                        println("更新了????")
                        val weakSuperview = WeakReference(view.parent(1) ?: return@UIKitView)
                        weakSuperview.get()?.apply {
                            this.backgroundColor = backgroundColor.toUIColor()
                            val size = view.frame.useContents { this.size }
                            if(size.width.toFloat().dp.value>0 && size.height.toFloat().dp.value>0){
                                www.value = if (state.isFillWidth) maxWidth else size.width.toFloat().dp
                                hhh.value = size.height.toFloat().dp
                                println("::::::: layoutFrameWithRect ==>width:${www.value} h=${hhh.value} text:::${
                                    Ksoup.parse(state.content).text().let {
                                        if (it.length > 10) {
                                            it.substring(0, 10)
                                        } else {
                                            it
                                        }
                                    }
                                }:::")
                                state.needMeasure = true
                                attributedLabel.value?.layouter = null
                                attributedLabel.value?.relayoutText()
                            }

                        }

                    }, onRelease = {
                    },
                    properties = UIKitInteropProperties(
                        interactionMode = UIKitInteropInteractionMode.Cooperative(),
                        isNativeAccessibilityEnabled = true
                    )
                )
            }
        }
    }

}

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class, ExperimentalForeignApi::class)
private fun BoxWithConstraintsScope.kvoScope(
    state: RichTextCompose,
    images: MutableList<String>,
    coroutineScope: CoroutineScope,
    hhh: MutableState<Dp>,
    attributedLabel: MutableState<DTAttributedLabel?>,
    isResize: Boolean,
    www: MutableState<Dp>
): DTAttributedTextContentViewDelegateProtocol {
    var isResize1 = isResize
    return object : NSObject(), DTAttributedTextContentViewDelegateProtocol,
        DTLazyImageViewDelegateProtocol {


        @ObjCAction
        @Suppress("unused")
        fun imageTap(sender: UITapGestureRecognizer) {
            val view = (sender.view as? DTLazyImageView) ?: return
            val imageURL = NSString.create(
                format = "%@",
                args = arrayOf(view.url)
            ).toString()
            state.clickImages(images, images.indexOf(imageURL))
        }

        /**
         * 图片占位
         */
        @ObjCSignatureOverride()
        override fun attributedTextContentView(
            attributedTextContentView: DTAttributedTextContentView?,
            viewForAttachment: DTTextAttachment?,
            frame: CValue<CGRect>
        ): UIView? {
            val attachment = (viewForAttachment ?: return null)

            val isImage =
                attachment.isKindOfClass(DTImageTextAttachment.`class`() as ObjCClass)
            val isTable = attachment is DTTableTextAttachment
            val isMathTex = attachment is MathTextAttachment
            val isHref = attachment is DTHrefAttachment
            if (isImage && !isTable) {
                val imageURL = NSString.create(
                    format = "%@",
                    args = arrayOf(viewForAttachment.contentURL)
                )
                imageURL.toString().apply {
                    if (!images.contains(this)) {
                        images.add(this)
                    }
                }
                val imageView = DTLazyImageView(frame = frame)
                imageView.delegate = this
                imageView.contentMode =
                    UIViewContentMode.UIViewContentModeScaleAspectFit
                imageView.setBackgroundColor(UIColor.whiteColor)
                imageView.image = viewForAttachment.image
                imageView.url = viewForAttachment.contentURL
                imageView.clipsToBounds = true
                imageView.setFrame(frame)
                imageView.setBackgroundColor(UIColor.yellowColor)
                imageView.userInteractionEnabled = true
                imageView.addGestureRecognizer(
                    UITapGestureRecognizer(
                        target = this,
                        action = NSSelectorFromString("imageTap:")
                    )
                )
                if (imageURL.containsString("gif")) {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            viewForAttachment.contentURL?.let {
                                val gifData =
                                    NSData.dataWithContentsOfURL(it)
                                withContext(Dispatchers.Main) {
                                    imageView.image = DTAnimatedGIFFromData(gifData)
                                }
                            }
                        }
                    }
                }
                return imageView
            } else if (isTable) {
                val table = attachment as DTTableTextAttachment
                val offset = table.sizeKeep.topLeft
                val size = table.sizeKeep.size
                val newRect = Rect(0F, 0F, size.width, size.height)
                table.sizeKeep = newRect
                table.offset = offset
                val gap =
                    newRect.height - frame.useContents { this.size.height }

                table.originalSize =
                    CGSizeMake(size.width.toDouble(), size.height.toDouble())
                table.displaySize =
                    CGSizeMake(size.width.toDouble(), size.height.toDouble())
                val newFrame = CGRectMake(
                    frame.useContents { this.origin.x } - offset.x,
                    frame.useContents { this.origin.y } - offset.y,
                    size.width.toDouble(), size.height.toDouble()
                )
                if (gap > 0) {
                    hhh.value += gap.dp

                }
                return TagRandererView(
                    frame = newFrame,
                    withAttachment = table
                ).apply {
                    coroutineScope.launch {
                        withContext(Dispatchers.Main) {
                            state.needMeasure = true
                            attributedLabel.value?.layouter = null
                            isResize1 = true
                            attributedLabel.value?.relayoutText()
                        }
                    }
                }
            } else if (isMathTex) {
                return mathView(attachment, frame)
            } else if (isHref) {
                val href = attachment as DTHrefAttachment
                val newRect = CGRectMake(
                    frame.useContents { origin.x },
                    frame.useContents { origin.y },
                    href.sizeKeep.width.toDouble(),
                    href.sizeKeep.height.toDouble()
                )
                href.displaySize = CGSizeMake(
                    href.sizeKeep.width.toDouble(),
                    href.sizeKeep.height.toDouble()
                )
                href.originalSize = CGSizeMake(
                    href.sizeKeep.width.toDouble(),
                    href.sizeKeep.height.toDouble()
                )
                val gap =
                    href.sizeKeep.height.toDouble() - frame.useContents { this.size.height }

                val rectTemp = CGRectMake(
                    0.0,
                    0.0,
                    href.sizeKeep.width.toDouble(),
                    href.sizeKeep.height.toDouble()
                )
                href.displaySize = CGSizeMake(
                    href.sizeKeep.width.toDouble(),
                    href.sizeKeep.height.toDouble()
                )
                href.originalSize = CGSizeMake(
                    href.sizeKeep.width.toDouble(),
                    href.sizeKeep.height.toDouble()
                )
                href.bounds = newRect
                val rootView = UIView(frame = newRect).apply {
                    setBackgroundColor(UIColor.clearColor)
                }
                rootView.addSubview(ClickButton(frame = rectTemp).apply {
                    val text = href.identifier
                    this.setFrame(rectTemp)
                    this.attributedText = text
                    this.textColor = UIColor.blueColor
                    this.backgroundColor = UIColor.clearColor
                    this.alpha = 0.5
                    this.userInteractionEnabled = true
                    this.clipsToBounds = true
                    val url = href.href
                    this.tapCallback = {
                        state.clickUrl(url)
                    }
                    this.addGestureRecognizer(
                        UITapGestureRecognizer(
                            target = this,
                            action = NSSelectorFromString("${ClickButton::tap.name}:")
                        )
                    )
                })
                if (gap > 0) {
                    hhh.value += gap.dp

                }
                return rootView.apply {
                    coroutineScope.launch {
                        withContext(Dispatchers.Main) {
                            rootView.setFrame(newRect)
                            state.needMeasure = true
                            attributedLabel.value?.layouter = null
                            isResize1 = true
                            attributedLabel.value?.relayoutText()
                        }
                    }
                }
            } else {
                println("viewForAttachment:${viewForAttachment}")
                return null
            }
        }

        private fun calculateActualRect(view: UIView): CValue<CGRect> {
            var maxX = 0.0
            var maxY = 0.0

            @Suppress("UNCHECKED_CAST")
            (view.subviews as List<UIView>).forEach { subview ->
                val frame = subview.frame
                // 更新最大 x 和 y
                val rightX =
                    frame.useContents { this.origin.x } + frame.useContents { this.size.width }
                if (rightX > maxX) {
                    maxX = rightX
                }
                val bottomY =
                    frame.useContents { this.origin.y } + frame.useContents { this.size.height }
                if (bottomY > maxY) {
                    maxY = bottomY
                }
            }

            // 计算包含所有子视图的实际 rect
            val width = maxX
            val height = maxY
            return CGRectMake(0.0, 0.0, width, height)
        }

        override fun attributedTextContentView(
            attributedTextContentView: DTAttributedTextContentView?,
            didDrawLayoutFrame: DTCoreTextLayoutFrame?,
            inContext: CGContextRef?
        ) {
            if (state.needMeasure.not()) {
                return
            }
            val atcv = attributedTextContentView ?: return
            val layout = didDrawLayoutFrame ?: return
            atcv.layoutFrameHeightIsConstrainedByBounds = false
            dispatch_async(dispatch_get_main_queue()) {
                val layoutTwice = atcv.layouter!!.layoutFrameWithRect(
                    frame = CGRectMake(
                        0.0,
                        0.0,
                        maxWidth.value.toDouble(),
                        CGFLOAT_HEIGHT_UNKNOWN.toDouble()
                    ), range = NSMakeRange(0u, atcv.attributedString?.length ?: 0u)
                ) ?: layout
                val size = layoutTwice.intrinsicContentFrame().useContents {
                    Size(
                        this.size.width.toFloat(),
                        this.size.height.toFloat()
                    )
                }
                val nWidth = if (state.isFillWidth) maxWidth else min(
                    size.width.dp,
                    maxWidth
                ).coerceAtLeast(10.dp)
                val nHeight = max(size.height.dp, 10.dp).coerceAtLeast(10.dp)
                if (www.value == nWidth && hhh.value == nHeight) {
                    return@dispatch_async
                }
                attributedLabel.value?.layouter = null
                www.value = nWidth
                hhh.value = nHeight

                println(
                    "::::::: layoutFrameWithRect width:${nWidth.value} h=${nHeight.value} text:::${
                        Ksoup.parse(state.content).text().let {
                            if (it.length > 10) {
                                it.substring(0, 10)
                            } else {
                                it
                            }
                        }
                    }:::"
                )
                attributedLabel.value?.relayoutText()
                if (state.isFillWidth.not()) {
                    state.needMeasure = false
                }
            }
        }

        private fun mathView(
            attachment: DTTextAttachment,
            frame: CValue<CGRect>
        ): UIView {
            val math = attachment as MathTextAttachment
            val latex = math.latex
            val oldRect = CGRectMake(
                frame.useContents { this.origin.x },
                frame.useContents { this.origin.y },
                maxWidth.value.toDouble(),
                maxWidth.value.toDouble()
            )
            val oldRectTemp = CGRectMake(
                0.0,
                0.0,
                maxWidth.value.toDouble(),
                maxWidth.value.toDouble()
            )
            val rootView = UIView(frame = oldRect).apply {
                setBackgroundColor(UIColor.clearColor)
            }
            val view =
                MTMathUILabel(frame = oldRectTemp).apply {
                    this.latex = latex
                    this.labelMode =
                        MTMathUILabelMode.kMTMathUILabelModeDisplay
                    this.fontSize = 20.0
                    this.textColor = UIColor.blackColor
                    this.sizeToFit()
                }
            val size = view.intrinsicContentSize.useContents {
                Size(
                    this.width.toFloat(),
                    this.height.toFloat()
                )
            }
            val newRect = CGRectMake(
                0.0,
                0.0,
                size.width.toDouble(),
                size.height.toDouble()
            )
            val newRect2 = CGRectMake(
                frame.useContents { this.origin.x },
                frame.useContents { this.origin.y },
                size.width.toDouble(),
                size.height.toDouble()
            )
            math.sizeKeep = Rect(
                0.0f,
                0.0f,
                size.width,
                size.height
            )

            view.setFrame(newRect)
            rootView.setFrame(newRect2)
            val gap =
                newRect.useContents { this.size.height } - frame.useContents { this.size.height }
                    .coerceAtLeast(0.0)

            math.displaySize = newRect.useContents {
                CGSizeMake(
                    this.size.width,
                    this.size.height
                )
            }
            math.originalSize = newRect.useContents {
                CGSizeMake(
                    this.size.width,
                    this.size.height
                )
            }
            if (gap > 0) {
                hhh.value += gap.dp
            }
            return rootView.apply {
                addSubview(view)
                coroutineScope.launch {
                    withContext(Dispatchers.Main) {
                        state.needMeasure = true
                        attributedLabel.value?.layouter = null
                        isResize1 = true
                        attributedLabel.value?.relayoutText()
                    }
                }
            }
        }


        /**
         * 懒加载获取图片大小
         */
        @Suppress("unchecked_cast")
        @ObjCSignatureOverride()
        override fun lazyImageView(
            lazyImageView: DTLazyImageView?,
            didChangeImageSize: CValue<CGSize>
        ) {
            
            val liv = lazyImageView ?: return
            val url = liv.url ?: return
            val imageSize = didChangeImageSize.useContents { this }

            val key = url.absoluteString ?: ""
            val predicate = NSPredicate.predicateWithFormat("contentURL == %@", args =arrayOf(url))
            println("textAttachmentsWithPredicate(predicate)::${attributedLabel}::${attributedLabel.value?.layoutFrame}:${attributedLabel.value?.layoutFrame?.textAttachmentsWithPredicate(predicate)}")
            val attachments =
                (attributedLabel.value?.layoutFrame?.textAttachmentsWithPredicate(predicate) as? List<DTTextAttachment>)
                    ?: emptyList()
            val noSizeList =
                attachments.fastMapIndexedNotNull { index, attachment ->
                    if (CGSizeEqualToSize(
                            attachment.originalSize,
                            CGSizeZero.readValue()
                        )
                    ) {
                        index to attachment
                    }
                    else {
                        imageSizeMap[key] =
                            Size(
                                imageSize.width.toFloat(),
                                imageSize.height.toFloat()
                            )
                        null
                    }
                }.toList()
            if (noSizeList.isEmpty()) return
            coroutineScope.launch {
                val completer = CompletableDeferred<Boolean>()
                withContext(Dispatchers.IO) {
                    val maxRect =
                        Rect(0.0f, 0.0f, maxWidth.value, CGFLOAT_HEIGHT_UNKNOWN)
                    val size = (if (imageSizeMap.containsKey(key).not()) {
                        configNoSizeImageView(key)
                    } else {
                        imageSizeMap[key]
                    }) ?: (Size(50.0f, 50.0f))
                    val imgSizeScale = size.height / size.width
                    val widthPx = maxRect.width
                    val heightPx = widthPx * imgSizeScale
                    val newSize = if (size.width > maxRect.width) {
                        Size(widthPx, heightPx)
                    } else {
                        Size(size.width, size.height)
                    }
                    imageSizeMap[key] = newSize
//                    var didUpdate = false
                    if(noSizeList.isEmpty()){
                        completer.complete(false)
                    }else{
                        try {
                            withTimeout(1000){
                                for ((index, attachment) in noSizeList) {
                                    withContext(Dispatchers.IO){
                                        withContext(Dispatchers.Main) {
                                            val cgSize = CGSizeMake(
                                                newSize.width.toDouble(),
                                                newSize.height.toDouble()
                                            )
                                            val gap =
                                                newSize.height.toDouble() - liv.frame.useContents { this.size.height }
                                            attachment.displaySize = cgSize
                                            attachment.originalSize = cgSize
                                            val frame = lazyImageView.frame.useContents { this }
                                            lazyImageView.setFrame(
                                                CGRectMake(
                                                    frame.origin.x,
                                                    frame.origin.y,
                                                    cgSize.useContents { this.width },
                                                    cgSize.useContents { this.height })
                                            )
                                            if (gap > 0) {
                                                hhh.value += gap.dp
                                                println("测量一下图片咯 gap:$gap")
                                            }
                                            if(index == noSizeList.size-1){
                                                if(completer.isCompleted.not()){
                                                    completer.complete(true)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }catch (e:Exception){
                            e.printStackTrace()
                            if(completer.isCompleted.not()){
                                completer.complete(false)
                            }
                        }
                    }
                    val didUpdate =completer.await()
                    if (didUpdate) {
                        withContext(Dispatchers.Main) {
                            println("测量一下图片咯")
                            state.needMeasure = true
                            attributedLabel.value?.layouter = null
                            isResize1 = true
                            attributedLabel.value?.relayoutText()
                        }
                    }
                }

            }
        }

        val imageSizeMap = mutableMapOf<String, Size>()

        /**
         * #pragma mark - Delegate：DTAttributedTextContentViewDelegate
         * 字符串中一些图片没有宽高，懒加载图片之后，在此方法中得到图片宽高
         * 这个把宽高替换原来的html,然后重新设置富文本
         *
         * 修改为获取图片宽高,保存在一个map中,然后设置图片宽高
         */
        suspend fun configNoSizeImageView(url: String): Size? {
            return withContext(Dispatchers.IO) {
                return@withContext NSURL.URLWithString(
                    URLString = url
                )?.let {
                    NSData.dataWithContentsOfURL(it)?.let {
                        val image = UIImage.imageWithData(it)
                        image?.size?.useContents {
                            this.let {
                                Size(it.width.toFloat(), it.height.toFloat())
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class DTAttributedTextContentViewRelayout(val value: Int) {
    RelayoutNever(0),
    RelayoutOnWidthChanged(1 shl 0),  // 1 << 0
    RelayoutOnHeightChanged(1 shl 1); // 1 << 1

    infix fun or(other: DTAttributedTextContentViewRelayout): Int {
        return this.value or other.value
    }
}


class ClickButton : UILabel {
    private constructor(coder: NSCoder) : super(coder)

    @OptIn(ExperimentalForeignApi::class)
    constructor(frame: CValue<CGRect>) : super(frame)

    var tapCallback: () -> Unit = {}

    @ObjCAction
    @Suppress("unused")
    fun tap(sender: UITapGestureRecognizer) {
        val view = (sender.view as? UILabel) ?: return
        val identifier = view.text ?: ""
        tapCallback()
    }

}

/**
 * Html转NSAttributedString
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun NSString.getAttributedStringWithHtml(
    maxWidth: Float,
    density: Density? = null,
    style: TextStyle
): NSAttributedString {
    val data = this.dataUsingEncoding(NSUTF8StringEncoding)
//    UIFontDescriptor *descriptor = [UIFontDescriptor preferredFontDescriptorWithTextStyle:UIFontTextStyleBody];
//    UIFont *font = [UIFont fontWithDescriptor:descriptor size:descriptor.pointSize];
//
//    NSDictionary *options = @{DTDefaultFontDescriptor: descriptor};


    val descriptor = UIFontDescriptor.preferredFontDescriptorWithTextStyle(UIFontTextStyleBody)
//    val font = UIFont.fontWithDescriptor(descriptor, descriptor.pointSize)
    val options = mapOf<Any?, Any?>(
        NSBaseURLDocumentOption to data,
        DTDefaultFontSize to NSNumber(style.fontSize.value.toFloat()),
        DTDefaultFontDescriptor to descriptor,
    )
    val stringBuilder = DTHTMLAttributedStringBuilder(
        hTML = data,
        options = options,
        documentAttributes = null
    )
    if (density != null) {
        //  表格处理
        stringBuilder.registerTagHandlers(
            tagHandlers = TableHandler(
                maxWidth,
                density
            ).allHandlers()
        )
//        stringBuilder.registerTagHandlers(tagHandlers = listOf(MathHandler(maxWidth, density)))
        stringBuilder.registerTagHandlers(
            tagHandlers = listOf(
                Base64ImageHandler(
                    maxWidth,
                    density
                )
            )
        )
        stringBuilder.registerTagHandlers(
            tagHandlers = listOf(
                ATagHandler(maxWidth),
                EMTagHandler(), SpanTagHandler(maxWidth, density)
            )
        )
    }
    val callbackBlock =
        options[DTWillFlushBlockCallBack] as? DTHTMLAttributedStringBuilderWillFlushCallback
    if (callbackBlock != null) {
        stringBuilder.setWillFlushCallback(callbackBlock)
    }
    try {
        val attributedString = stringBuilder.generatedAttributedString()
        return attributedString ?: NSAttributedString.create(string = "")
    } catch (e: Exception) {
        e.printStackTrace()
        return NSAttributedString.create(string = "")
    }
}

/**
 * 使用HtmlString,和最大左右间距，计算视图的高度
 */
@OptIn(ExperimentalForeignApi::class)
internal fun NSAttributedString.getAttributedTextHeightHtml(
    maxRect: Rect
): Size {
    val layouter = DTCoreTextLayouter(attributedString = this)
    val entireString = NSMakeRange(0u, this.length)
    val layoutFrame = layouter.layoutFrameWithRect(
        CGRectMake(
            x = maxRect.left.toDouble(),
            y = maxRect.top.toDouble(),
            width = maxRect.width.toDouble(),
            height = maxRect.height.toDouble()
        ),
        range = entireString
    )
    val measureSize = layoutFrame?.frame?.useContents {
        Size(
            this.size.width.toFloat(),
            this.size.height.toFloat()
        )
    }
    return measureSize ?: Size.Zero
}

