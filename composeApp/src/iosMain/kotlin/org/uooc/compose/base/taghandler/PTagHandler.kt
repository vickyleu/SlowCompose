package org.uooc.compose.base.taghandler

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import org.uooc.compose.utils.toCGSize
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.CoreText.kCTTextAlignmentLeft
import platform.Foundation.NSData
import platform.Foundation.NSMakeRange
import platform.Foundation.NSMutableAttributedString
import platform.Foundation.addAttribute
import platform.Foundation.create
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.NSStringDrawingUsesFontLeading
import platform.UIKit.NSStringDrawingUsesLineFragmentOrigin
import platform.UIKit.NSUnderlineStyleAttributeName
import platform.UIKit.NSUnderlineStyleSingle
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.boundingRectWithSize
import uooc.DTCoreText.DTAnchorHTMLElement
import uooc.DTCoreText.DTCoreTextParagraphStyle
import uooc.DTCoreText.DTHTMLElement
import uooc.DTCoreText.DTHTMLElementDisplayStyleInline
import uooc.DTCoreText.DTTextAttachment
import uooc.DTCoreText.DTTextAttachmentVerticalAlignmentBaseline
import uooc.DTCoreText.DTTextHTMLElement
import uooc.DTCoreText.UoocTagHandler

//class PTagHandler : UoocTagHandler("p") {
//    override fun handleEndTag(currentTag: DTHTMLElement) {
//
//    }
//
//
//    override fun handleStartTag(currentTag: DTHTMLElement) {
//        println("handleStartTag:")
//
//    }
//}
class EMTagHandler : UoocTagHandler("em") {
    override fun handleEndTag(currentTag: DTHTMLElement) {
        currentTag.letterSpacing = 0.0
        currentTag.displayStyle = DTHTMLElementDisplayStyleInline
        currentTag.setParagraphStyle(DTCoreTextParagraphStyle().apply {
            setAlignment(kCTTextAlignmentLeft)
            setParagraphSpacing(0.0)
            setParagraphSpacingBefore(0.0)
        })
        currentTag.preserveNewlines = false
    }

    override fun handleStartTag(currentTag: DTHTMLElement) {
    }
}

class SpanTagHandler(maxWidth: Float, density: Density) : UoocTagHandler("span") {
   private val mathHandler = MathHandler(maxWidth, density)
    override fun handleEndTag(currentTag: DTHTMLElement) {
        mathHandler.handleEndTag(currentTag)
        currentTag.letterSpacing = 0.0
        currentTag.displayStyle = DTHTMLElementDisplayStyleInline
        currentTag.setParagraphStyle(DTCoreTextParagraphStyle().apply {
            setAlignment(kCTTextAlignmentLeft)
            setParagraphSpacing(0.0)
            setParagraphSpacingBefore(0.0)
        })
        currentTag.preserveNewlines = false
    }

    override fun handleStartTag(currentTag: DTHTMLElement) {
        mathHandler.handleStartTag(currentTag)
    }
}

class DTHrefAttachment(
    href: String,
    identifier: NSMutableAttributedString,
    element: DTHTMLElement,
    options: Map<Any?, *>?
) : DTTextAttachment(
    element,
    options
) {
    var href: String = href
        private set
    var identifier: NSMutableAttributedString = identifier
        private set

    var sizeKeep = Size(0.0f, 0.0f)


    @Deprecated(
        "Use constructor instead",
        replaceWith = ReplaceWith("DTTextAttachment(contentData, ofType)"),
        level = DeprecationLevel.ERROR
    )
    override fun initWithData(contentData: NSData?, ofType: String?): DTTextAttachment {
        return this
    }

    override fun originalSize(): CValue<CGSize> {
        return sizeKeep.let {
            CGSizeMake(it.width.toDouble(), it.height.toDouble())
        }
    }

    override fun displaySize(): CValue<CGSize> {
        return sizeKeep.let {
            CGSizeMake(it.width.toDouble(), it.height.toDouble())
        }
    }
}


class ATagHandler(private val maxWidth: Float) : UoocTagHandler("a") {
    override fun handleEndTag(currentTag: DTHTMLElement) {
        var current = currentTag

        (current as? DTAnchorHTMLElement)?.apply {
            val parentTag = current.parentElement()
            val nodes = (current.childNodes
                ?: emptyList<DTHTMLElement>()).toMutableList() as MutableList<DTHTMLElement>
            if (nodes.filterIsInstance<DTTextHTMLElement>().isNotEmpty()) {
                val textElement = nodes.filterIsInstance<DTTextHTMLElement>().first()
                val index = nodes.indexOf(textElement)
                val others = nodes.filterIndexed { idx, _ -> idx != index }
                val attr = this.attributes ?: emptyMap<Any?, Any>()
                if (attr.containsKey("href")) {
                    val href = (attr["href"]?.toString() ?: "")
                    val identifier = "${textElement.text() ?: attr["title"]}"
                    if (href.isNotEmpty()) {
                        val id = identifier.ifEmpty { "按钮" }
                        val attributedString = NSMutableAttributedString.create(
                            string = id, attributes = mapOf(
                                NSFontAttributeName to UIFont.italicSystemFontOfSize(16.0),
                                NSForegroundColorAttributeName to UIColor.blueColor
                            )
                        ).apply {
                            addAttribute(
                                NSUnderlineStyleAttributeName,
                                value = NSUnderlineStyleSingle,
                                range = NSMakeRange(0u, id.length.toULong())
                            )
                        }

                        val rect = Rect(left = 0.0f, top = 0.0f, maxWidth, maxWidth)
                        val size = attributedString.boundingRectWithSize(
                            size = rect.size.toCGSize(),
                            context = null,
                            options = NSStringDrawingUsesLineFragmentOrigin or NSStringDrawingUsesFontLeading
                        ).useContents {
                            Size(
                                this.size.width.toFloat(),
                                this.size.height.toFloat()
                            )
                        }
                        val newTextElement = DTHTMLElement("a", null).apply {
                            displayStyle = DTHTMLElementDisplayStyleInline
                            setParagraphStyle(DTCoreTextParagraphStyle().apply {
                                setAlignment(kCTTextAlignmentLeft)
                                setParagraphSpacing(0.0)
                                setParagraphSpacingBefore(0.0)
                            })
                            preserveNewlines = false
                            setSize(size.toCGSize())
                            textAttachment =
                                DTHrefAttachment(href, attributedString, textElement, null).apply {
                                    this.verticalAlignment =
                                        DTTextAttachmentVerticalAlignmentBaseline
                                    this.sizeKeep = size
                                }
                        }
                        nodes[index] = newTextElement
                        others.forEach {
                            it.displayStyle = DTHTMLElementDisplayStyleInline
                            it.setParagraphStyle(DTCoreTextParagraphStyle().apply {
                                setAlignment(kCTTextAlignmentLeft)
                                setParagraphSpacing(0.0)
                                setParagraphSpacingBefore(0.0)
                            })
                            it.preserveNewlines = false
                        }
                        if (parentTag != null) {
                            val newParentNodes = (parentTag.childNodes
                                ?: emptyList<DTHTMLElement>()).toMutableList() as MutableList<DTHTMLElement>
                            val wrapper = DTHTMLElement("span", null).apply {
                                nodes.forEach {
                                    this.addChildNode(it)
                                }
//                                this.setText("  ")
                                this.setParentNode(parentTag)
                                this.setBeforeContent("")

                                this.displayStyle = DTHTMLElementDisplayStyleInline
                                this.setParagraphStyle(DTCoreTextParagraphStyle().apply {
                                    setAlignment(kCTTextAlignmentLeft)
                                    setParagraphSpacing(0.0)
                                    setParagraphSpacingBefore(0.0)
                                })
                                this.setSize(CGSizeMake(10.0, 10.0))
                                this.preserveNewlines = false
                            }
                            newParentNodes[newParentNodes.indexOf(current)] = wrapper
                            parentTag.removeAllChildNodes()
                            newParentNodes.forEach {
                                parentTag.addChildNode(it)
                            }
                            parentTag.displayStyle = DTHTMLElementDisplayStyleInline
                            parentTag.setParagraphStyle(DTCoreTextParagraphStyle().apply {
                                setAlignment(kCTTextAlignmentLeft)
                                setParagraphSpacing(0.0)
                                setParagraphSpacingBefore(0.0)
                            })
                        }
                        else {
                            currentTag.removeAllChildNodes()
                            nodes.forEach {
                                currentTag.addChildNode(it)
                            }
                            currentTag.displayStyle = DTHTMLElementDisplayStyleInline
                            currentTag.setParagraphStyle(DTCoreTextParagraphStyle().apply {
                                setAlignment(kCTTextAlignmentLeft)
                                setParagraphSpacing(0.0)
                                setParagraphSpacingBefore(0.0)
                            })
                            currentTag.preserveNewlines = false
                            println("currentTag: ${(currentTag.childNodes as List<DTHTMLElement>).mapIndexed { index: Int, dthtmlElement: DTHTMLElement ->
                                "index: $index, element: ${dthtmlElement.name} ==${dthtmlElement.text()}==(${dthtmlElement.attributedString()})"
                            }.joinToString()}")
                        }
                    }
                }
            } else {
                val attr = this.attributes ?: emptyMap<Any?, Any>()
                if (attr.containsKey("href")) {
                    val href = (attr["href"]?.toString() ?: "")
                    val identifier = "${attr["title"]}"
                    if (href.isNotEmpty()) {
                        val id = identifier.ifEmpty { "按钮" }
                        val attributedString = NSMutableAttributedString.create(
                            string = id, attributes = mapOf(
                                NSFontAttributeName to UIFont.italicSystemFontOfSize(16.0),
                                NSForegroundColorAttributeName to UIColor.blueColor
                            )
                        ).apply {
                            addAttribute(
                                NSUnderlineStyleAttributeName,
                                value = NSUnderlineStyleSingle,
                                range = NSMakeRange(0u, id.length.toULong())
                            )
                        }

                        val rect = Rect(left = 0.0f, top = 0.0f, maxWidth, maxWidth)
                        val size = attributedString.boundingRectWithSize(
                            size = rect.size.toCGSize(),
                            context = null,
                            options = NSStringDrawingUsesLineFragmentOrigin or NSStringDrawingUsesFontLeading
                        ).useContents {
                            Size(
                                this.size.width.toFloat(),
                                this.size.height.toFloat()
                            )
                        }
                        currentTag.textAttachment =
                            DTHrefAttachment(href, attributedString, currentTag, null).apply {
                                this.verticalAlignment =
                                    DTTextAttachmentVerticalAlignmentBaseline
                                this.sizeKeep = size
                            }
//                            currentTag.size = size.toCGSize()

                        currentTag.size =
                            CGSizeMake(size.width.toDouble(), size.height.toDouble())
                        currentTag.displayStyle = DTHTMLElementDisplayStyleInline
                        currentTag.setParagraphStyle(DTCoreTextParagraphStyle().apply {
                            setAlignment(kCTTextAlignmentLeft)
                            setParagraphSpacing(0.0)
                            setParagraphSpacingBefore(0.0)
                        })
                        currentTag.preserveNewlines = false

                    }
                }
            }

        }
    }

    override fun handleStartTag(currentTag: DTHTMLElement) {
        // No need to implement for this case
    }
}

