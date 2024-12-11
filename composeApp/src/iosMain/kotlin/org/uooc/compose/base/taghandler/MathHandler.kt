@file:OptIn(ExperimentalForeignApi::class)

package org.uooc.compose.base.taghandler

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.util.fastMapNotNull
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.CoreText.kCTTextAlignmentLeft
import platform.Foundation.NSData
import uooc.DTCoreText.DTCoreTextParagraphStyle
import uooc.DTCoreText.DTHTMLElement
import uooc.DTCoreText.DTHTMLElementDisplayStyleInline
import uooc.DTCoreText.DTTextAttachment
import uooc.DTCoreText.DTTextAttachmentVerticalAlignmentBaseline
import uooc.DTCoreText.DTTextHTMLElement
import uooc.DTCoreText.UoocTagHandler

class MathHandler(private val maxWidth: Float, private val density: Density) :
    UoocTagHandler("span") {

    private var findMathTex = false

    @OptIn(ExperimentalForeignApi::class)
    override fun handleStartTag(currentTag: DTHTMLElement) {
        currentTag.attributeForKey("class")?.let {
            if (it == "math-tex") {
                findMathTex = true
            }
        }
    }

    override fun handleEndTag(currentTag: DTHTMLElement) {
        if (findMathTex) {
            val list = (currentTag.childNodes as? List<DTTextHTMLElement>) ?: emptyList()
            val mathTex = (list.fastMapNotNull {
                it.text()?.trim()
            }.firstOrNull {
                it.startsWith("\\(") && it.endsWith("\\)")
            } ?: "").trim().removePrefix("\\(").removeSuffix("\\)")



            mathTex.let {
                if (mathTex.isNotEmpty()) {
                    val parent = currentTag.parentElement()
                    if(parent!=null){
                        val newTextElement = DTHTMLElement("math", null).apply {
                            displayStyle = DTHTMLElementDisplayStyleInline
                            setParagraphStyle(DTCoreTextParagraphStyle().apply {
                                setAlignment(kCTTextAlignmentLeft)
                                setParagraphSpacing(0.0)
                                setParagraphSpacingBefore(0.0)
                            })
                            preserveNewlines = false
                            setSize(CGSizeMake(10.0, 10.0))
                            textAttachment =MathTextAttachment(mathTex,maxWidth, currentTag, null).apply {
                                this.verticalAlignment = DTTextAttachmentVerticalAlignmentBaseline
                                this.sizeKeep = Rect(0f, 0f, 10f, 10f)
                            }
                        }
                        val nodes = (parent.childNodes?: emptyList<DTHTMLElement>()).toMutableList() as MutableList<DTHTMLElement>
                        val index = nodes.indexOf(currentTag)
                        nodes[index] = newTextElement
                        parent.removeAllChildNodes()
                        nodes.forEach {
                            parent.addChildNode(it)
                        }
                    }else{
                        currentTag.textAttachment = MathTextAttachment(mathTex,maxWidth, currentTag, null).apply {
                            this.verticalAlignment = DTTextAttachmentVerticalAlignmentBaseline
                            this.sizeKeep = Rect(0f, 0f, 10f, 10f)
                        }
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
            findMathTex = false
        }
    }
}

class MathTextAttachment : DTTextAttachment {
    var latex: String = ""
        private set

    private var maxWidth: Float = 0f

    constructor(
        latex: String,
        maxWidth: Float,
        element: DTHTMLElement?,
        options: Map<Any?, *>?
    ) : super(
        element,
        options
    ) {
        this.latex = latex
        this.maxWidth = maxWidth
    }

    var sizeKeep = Rect.Zero

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

    @Deprecated(
        "Use constructor instead",
        replaceWith = ReplaceWith("DTTextAttachment(contentData, ofType)"),
        level = DeprecationLevel.ERROR
    )
    override fun initWithData(contentData: NSData?, ofType: String?): DTTextAttachment {
        return this
    }

    private constructor(data: NSData?, ofType: String?) : super(data, ofType)

}
