package org.uooc.compose.base.taghandler

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import org.uooc.compose.base.convertToLatex
import org.uooc.compose.utils.toCGSize
import platform.CoreGraphics.CGSizeMake
import platform.CoreText.kCTTextAlignmentLeft
import uooc.DTCoreText.DTCoreTextParagraphStyle
import uooc.DTCoreText.DTHTMLElement
import uooc.DTCoreText.DTHTMLElementDisplayStyleInline
import uooc.DTCoreText.DTTextAttachmentVerticalAlignmentBaseline
import uooc.DTCoreText.DTTextHTMLElement
import uooc.DTCoreText.UoocTagHandler

class Base64ImageHandler(private val maxWidth: Float, private val density: Density): UoocTagHandler("img")  {
    private var isFoundLatex=false
    override fun handleEndTag(currentTag: DTHTMLElement) {
        if(isFoundLatex){
            val latex = (currentTag.attributeForKey("data-latex")?:"").let {
                convertToLatex(it)
            }
            if(latex.isNotEmpty()){
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
                        textAttachment =MathTextAttachment(latex,maxWidth, currentTag, null).apply {
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
                    currentTag.textAttachment = MathTextAttachment(latex,maxWidth, currentTag, null).apply {
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
        isFoundLatex=false
    }

    override fun handleStartTag(currentTag: DTHTMLElement) {
       if(currentTag.attributeForKey("class")=="kfformula" && currentTag.attributes?.containsKey("data-latex")==true){
           isFoundLatex = true
       }
    }
}
