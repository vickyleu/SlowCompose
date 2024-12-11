@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package org.uooc.compose.base.taghandler

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.uooc.compose.utils.toCGRect
import platform.CoreGraphics.CGContextClearRect
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGContextRef
import platform.CoreGraphics.CGContextSetCMYKFillColor
import platform.CoreGraphics.CGContextSetFillColorWithColor
import platform.CoreGraphics.CGContextSetLineWidth
import platform.CoreGraphics.CGContextSetStrokeColorWithColor
import platform.CoreGraphics.CGContextStrokeRect
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.CoreText.kCTTextAlignmentLeft
import platform.Foundation.NSAttributedString
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.NSLineBreakByWordWrapping
import platform.UIKit.NSMutableParagraphStyle
import platform.UIKit.NSParagraphStyleAttributeName
import platform.UIKit.NSStringDrawingUsesLineFragmentOrigin
import platform.UIKit.NSTextAlignmentLeft
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.boundingRectWithSize
import platform.UIKit.drawInRect
import platform.UIKit.size
import uooc.DTCoreText.DTCoreTextParagraphStyle
import uooc.DTCoreText.DTHTMLElement
import uooc.DTCoreText.DTHTMLElementDisplayStyleInline
import uooc.DTCoreText.DTImageTextAttachment
import uooc.DTCoreText.DTTextAttachmentHTMLPersistenceProtocol
import uooc.DTCoreText.DTTextHTMLElement
import uooc.DTCoreText.TagHandlerProtocol
import uooc.DTCoreText.UoocTagHandler
import kotlin.collections.set
import kotlin.experimental.ExperimentalNativeApi
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("DTTableTextAttachment")
/**
 * Table
 */
class DTTableTextAttachment(
    private val maxWidth: Float,
    private val element: DTHTMLElement,
    options: Map<Any?, *>?
) : DTImageTextAttachment(
    element,
    options
), DTTextAttachmentHTMLPersistenceProtocol {
    @Deprecated(
        "Use constructor instead",
        replaceWith = ReplaceWith("DTImageTextAttachment(contentData, ofType)"),
        level = DeprecationLevel.ERROR
    )
    override fun initWithData(contentData: NSData?, ofType: String?): DTImageTextAttachment {
        return this
    }

    var sizeKeep = Rect.Zero
    var offset = Offset.Zero

    override fun drawInRect(rect: CValue<CGRect>, context: CGContextRef?) {
        // 确保 context 不为 null
        context?.let {
//            CGContextSetFillColorWithColor(context, UIColor.redColor.CGColor)
//            CGContextFillRect(it, rect)
            var lastAttachment: DTTableColumnTextAttachment? = null
            this.childNodes.forEachIndexed { index, attachment ->
                //这里是画 DTTableColumnTextAttachment
                val column = attachment as DTTableColumnTextAttachment
                val r = column.sizeKeep
                column.offset = offset
                column.index=index+((lastAttachment?.let { it.index+(it.childNode.size -1)}?:0)).coerceAtLeast(0)
                column.drawInRect(r.let {
                    CGRectMake(
                        x = it.left.toDouble(),
                        y = it.top.toDouble(),
                        it.size.width.toDouble(),
                        it.size.height.toDouble()
                    )
                }, context)
                lastAttachment = attachment
            }
        } ?: throw IllegalArgumentException("context is null")
    }


    override fun stringByEncodingAsHTML(): String {
        return "<table></table>"
    }

    private val childNodes = mutableListOf<DTImageTextAttachment>()
    fun addAll(childNode: List<Any>) {
        try {
            this.childNodes.addAll(childNode.filterIsInstance<DTImageTextAttachment>())
            adjustColumnWidths(maxWidth)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalNativeApi::class)
    fun adjustColumnWidths(maxWidth: Float) {
        val maxWidthModify = maxWidth
        if(childNodes.isEmpty())return
        val rowCount = (childNodes.first() as DTTableColumnTextAttachment).childNode.size// 获取列（row）的数量
        val columnCount = childNodes.size // 获取行（column）的数量
        val columnWidth = maxWidthModify / rowCount.toFloat() // 每列的宽度是固定的

        // 重新计算每行中每列的最大高度
        val columnMeasureHeight = MutableList(columnCount) { 0.0 }
        childNodes.forEachIndexed { colIndex, columnAttachment ->
            val column = columnAttachment as DTTableColumnTextAttachment
            column.childNode.forEachIndexed { rowIndex, rowAttachment ->
                var maxRowHeight = 0.0
                rowAttachment.childNode.let { (_, attributedString) ->
                    val textSize = attributedString.boundingRectWithSize(
                        CGSizeMake(columnWidth.toDouble() - 20.0 /*左右间距*/, Double.MAX_VALUE),
                        NSStringDrawingUsesLineFragmentOrigin,
                        context = null
                    ).useContents { Size(size.width.toFloat(), size.height.toFloat()) }
                    val textHeight = textSize.height.toDouble() + 10.0 /*上下间距*/
                    maxRowHeight = maxOf(maxRowHeight, textHeight)
                }
                columnMeasureHeight[colIndex] = maxOf(columnMeasureHeight[colIndex], maxRowHeight) // 更新该行的最大高度
            }
        }

        // 更新每个子节点的 rect，使用固定的列宽并设置行高
        var currentY = 0.0 // 用于跟踪当前的 Y 坐标
        childNodes.forEachIndexed { colIndex, columnAttachment ->
            val column = columnAttachment as DTTableColumnTextAttachment
            var currentX = 0.0 // 用于跟踪当前的 X 坐标
            column.childNode.forEachIndexed { rowIndex, rowAttachment ->
                val rowHeight = columnMeasureHeight[colIndex] // 使用该行的最大高度
                // 更新单元格的 rect
                rowAttachment.childNode.let { (rect, _) ->
                    rect.setNewRect(
                        Rect(
                            left = currentX.toFloat() + 10,
                            top = currentY.toFloat() + 5,
                            right = currentX.toFloat() + (columnWidth - 20),
                            bottom = currentY.toFloat() + rowHeight.toFloat() - 10
                        )
                    )
                }

                rowAttachment.sizeKeep = Rect(
                    left = currentX.toFloat(),
                    top = currentY.toFloat(),
                    right = currentX.toFloat() + columnWidth,
                    bottom = currentY.toFloat() + rowHeight.toFloat()
                )

                currentX += columnWidth // 更新 X 坐标
            }

            currentY += columnMeasureHeight[colIndex] // 更新 Y 坐标
        }

        // 计算整个表格的大小
        val totalHeight = columnMeasureHeight.sum()
        this.originalSize = CGSizeMake(maxWidthModify.toDouble(), totalHeight)
        this.bounds = CGRectMake(
            this.bounds.useContents { origin.x },
            this.bounds.useContents { origin.y },
            maxWidthModify.toDouble(),
            totalHeight
        )

        this.sizeKeep = Rect(
            left = this.bounds.useContents { origin.x }.toFloat(),
            top = this.bounds.useContents { origin.y }.toFloat(),
            right = this.bounds.useContents { origin.x }.toFloat() + maxWidthModify,
            bottom = this.bounds.useContents { origin.y }.toFloat() + totalHeight.toFloat()
        )
    }

    fun adjustColumnWidths1(maxWidth: Float) {
        val maxWidthModify =maxWidth

        // 重新计算每一行的高度
        val columnMeasureHeight = List(childNodes.size){0.0}.toMutableList()
        childNodes.forEachIndexed { colIndex, columnAttachment ->
            val column = columnAttachment as DTTableColumnTextAttachment
            val adjustedWidth = maxWidthModify/column.childNode.size.toFloat()
            column.childNode.forEachIndexed { rowIndex, rowAttachment ->
                var maxRowHeight = 0.0
                rowAttachment.childNode.let { (_, attributedString) ->
                    val textSize = attributedString.boundingRectWithSize(
                        CGSizeMake(adjustedWidth.toDouble()-20.0/*左右间距*/, Double.MAX_VALUE),
                        NSStringDrawingUsesLineFragmentOrigin,
                        context = null
                    ).useContents { Size(size.width.toFloat(),size.height.toFloat()) }
                    val textHeight = textSize.height.toDouble()/*上下间距*/
                    maxRowHeight = maxOf(maxRowHeight, textHeight)
                    columnMeasureHeight[colIndex]=maxRowHeight+10.0
                }
            }
        }
        // 更新每个子节点的 rect，使用统一的列宽并设置行高
        childNodes.forEachIndexed { colIndex, columnAttachment ->
            val column = columnAttachment as DTTableColumnTextAttachment
            val adjustedWidth = maxWidthModify/column.childNode.size.toFloat()
            column.childNode.forEachIndexed { rowIndex, rowAttachment ->
                val rowHeight = columnMeasureHeight[colIndex]
                // 更新单元格 rect
                rowAttachment.childNode.let { (rect, _) ->
                    rect.setNewRect(Rect(
                        left = rect.tokRect().left+10,
                        top = rect.tokRect().top+5,
                        right = (rect.tokRect().left) + (adjustedWidth-20),
                        bottom = (rect.tokRect().top) + (rowHeight.toFloat()-10)
                    ))
                }

                // 打印每个单元格的宽高
                print("| 宽${adjustedWidth.toInt()},高${rowHeight.toInt()} |")

                // 设置行的宽度和高度
                val rowOrigin = rowAttachment.bounds.useContents { this.origin }
                rowAttachment.bounds =
                    CGRectMake(rowOrigin.x, rowOrigin.y, adjustedWidth.toDouble(), rowHeight)
                rowAttachment.originalSize = CGSizeMake(adjustedWidth.toDouble(), rowHeight)
            }
        }
        // 设置列的宽度和高度
        childNodes.forEachIndexed { colIndex, columnAttachment ->
            val column = columnAttachment as DTTableColumnTextAttachment
            val origin = column.bounds.useContents { this.origin }
            // 计算列的总高度
            val columnTotalHeight = columnMeasureHeight.sum()
            val columnTotalWidth = column.childNode.map {
                it.bounds.useContents { size.width }
            }.sum()

            column.originalSize = CGSizeMake(columnTotalWidth, columnTotalHeight)
            // 更新列的 bounds
            column.bounds = CGRectMake(origin.x, origin.y, columnTotalWidth, columnTotalHeight)

        }
        // 处理每个单元格的 x 和 y 坐标
        var currentY = 0.0 // 用于跟踪当前的 Y 坐标
        childNodes.forEachIndexed { colIndex, columnAttachment ->
            val column = columnAttachment as DTTableColumnTextAttachment
            val adjustedWidth = maxWidthModify/column.childNode.size.toFloat()
            var currentX = 0.0 // 用于跟踪当前的 X 坐标
            column.childNode.forEachIndexed { rowIndex, rowAttachment ->
                val rowHeight = columnMeasureHeight[colIndex]
                // 更新单元格的 rect
                rowAttachment.childNode.let { (rect, _) ->
                    rect.setNewRect(Rect(
                        left = currentX.toFloat()+10,
                        top = currentY.toFloat()+5,
                        right = currentX.toFloat() + (adjustedWidth-20),
                        bottom = currentY.toFloat() + (rowHeight.toFloat()-10)
                    ))
                }
                rowAttachment.sizeKeep = Rect(
                    left =currentX.toFloat(),
                    top =currentY.toFloat(),
                    right =currentX.toFloat()+adjustedWidth,
                    bottom =currentY.toFloat()+rowHeight.toFloat()
                )
                // 打印单元格的 x, y, 宽度和高度
                currentX += adjustedWidth.toDouble() // 更新 x 坐标
                if (rowIndex == column.childNode.size - 1) {
                    currentY += rowHeight // 更新 y 坐标
                }
                if (rowIndex == column.childNode.size - 1) {
                    //最后一个单元格
                    column.originalSize = CGSizeMake(currentX, rowHeight)
                    column.bounds = CGRectMake(
                        (column.bounds.useContents { this.origin.x }),
                        column.bounds.useContents { this.origin.y }+(currentY-rowHeight).coerceAtLeast(0.0),
                        currentX,
                        rowHeight
                    )

                    column.sizeKeep = Rect(
                        left=column.bounds.useContents { this.origin.x }.toFloat(),
                        top =column.bounds.useContents { this.origin.y }.toFloat(),
                        right = column.bounds.useContents { this.origin.x }.toFloat()+currentX.toFloat(),
                        bottom= column.bounds.useContents { this.origin.y }.toFloat()+rowHeight.toFloat()
                    )
                }
            }
        }

        val calculateSize = childNodes.lastOrNull()?.let {
            val column = it as DTTableColumnTextAttachment
            Size(column.sizeKeep.left+column.sizeKeep.width, column.sizeKeep.top+column.sizeKeep.height)
        } ?: Size(0f, 0f)

        this.originalSize =
            CGSizeMake(calculateSize.width.toDouble(), calculateSize.height.toDouble())
        this.bounds =
            CGRectMake(this.bounds.useContents { origin.x }, this.bounds.useContents { origin.y },
                calculateSize.width.toDouble(), calculateSize.height.toDouble()
            )

        this.sizeKeep = Rect(
            left=this.bounds.useContents { origin.x }.toFloat(),
            top = this.bounds.useContents { origin.y }.toFloat(),
            right = this.bounds.useContents { origin.x }.toFloat()+calculateSize.width,
            bottom= this.bounds.useContents { origin.y }.toFloat()+calculateSize.height
        )
    }
    override fun originalSize(): CValue<CGSize> {
        return CGSizeMake(sizeKeep.width.toDouble(), sizeKeep.height.toDouble())
    }

    override fun displaySize(): CValue<CGSize> {
        return CGSizeMake(sizeKeep.width.toDouble(), sizeKeep.height.toDouble())
    }


    override fun description(): String {
        return "DTTableTextAttachment@${hashCode()}"
    }
}

internal class MutableRect( x: Double,  y: Double,  width: Double,  height: Double) {
    private var rect = Rect(left = x.toFloat(), top = y.toFloat(), right = x.toFloat()+width.toFloat(), bottom = y.toFloat()+height.toFloat())
    fun tokRect(): Rect {
        return rect
    }

    fun setNewRect(rect: Rect) {
        this.rect = rect
    }
    fun toCGRect(): CValue<CGRect> {
        return CGRectMake(rect.left.toDouble(), rect.top.toDouble(), rect.width.toDouble(), rect.height.toDouble())
    }

}

@OptIn(ExperimentalObjCName::class)
@ObjCName("DTTableTextAttachment")
/**
 * TH or TD
 */
class DTTableRowTextAttachment(val flag: String, element: DTHTMLElement, options: Map<Any?, *>?) :
    DTImageTextAttachment(
        element,
        options
    ) {
    @Deprecated(
        "Use constructor instead",
        replaceWith = ReplaceWith("DTImageTextAttachment(contentData, ofType)"),
        level = DeprecationLevel.ERROR
    )
    override fun initWithData(contentData: NSData?, ofType: String?): DTImageTextAttachment {
        return this
    }

    var sizeKeep = Rect.Zero
//    var offset = Offset.Zero
    internal var childNode = Pair(MutableRect(0.0, 0.0, 0.0, 0.0), NSAttributedString.create(""))

    fun add(attachment: NSAttributedString?) {
        this.childNode = ((attachment ?: return).let { att ->
            val paragraphStyle = NSMutableParagraphStyle()
            paragraphStyle.setLineBreakMode(NSLineBreakByWordWrapping)
            paragraphStyle.setAlignment(NSTextAlignmentLeft)
            // 获取现有的属性并复制为可变字典
            val newAttributes = att.attributesAtIndex(0u, effectiveRange = null).toMutableMap()
            // 添加/替换段落样式
            newAttributes[NSParagraphStyleAttributeName] = paragraphStyle
            // 创建新的 NSAttributedString
            NSAttributedString.create(
                string = att.string,
                attributes = newAttributes
            )
        }.let {
            MutableRect(
                0.0,
                0.0,
                it.size().useContents { width },
                it.size().useContents { height }) to it
        })
    }

    override fun stringByEncodingAsHTML(): String {
        return "<th></th>"
    }

    override fun description(): String {
        return "DTTableRowTextAttachment@${hashCode()}"
    }

    private var newSizeKeepText:Rect?=null
    private var newSizeKeep:Rect?=null
     var index = 0

    override fun displaySize(): CValue<CGSize> {
        return CGSizeMake(sizeKeep.width.toDouble(), sizeKeep.height.toDouble())
    }

    override fun originalSize(): CValue<CGSize> {
        return CGSizeMake(sizeKeep.width.toDouble(), sizeKeep.height.toDouble())
    }
    override fun drawInRect(rect: CValue<CGRect>, context: CGContextRef?) {
        context?.let {
            it.usePinned { pinnedContext ->
                val ctx = pinnedContext.get()

                if (newSizeKeepText == null) {
                    val attributedString = childNode.second
                    val textSize = attributedString.boundingRectWithSize(
                        CGSizeMake(sizeKeep.width.toDouble(), sizeKeep.height.toDouble()),
                        options = NSStringDrawingUsesLineFragmentOrigin,
                        context = null
                    ).useContents {
                        Size(this.size.width.toFloat(), this.size.height.toFloat())
                    }

                    var x = sizeKeep.left + (sizeKeep.width - textSize.width) / 2.0
                    var y = sizeKeep.top + (sizeKeep.height - textSize.height) / 2.0
                    val right = x + textSize.width
                    val bottom = y + textSize.height

                    newSizeKeepText = Rect(x.toFloat(), y.toFloat(), right.toFloat(), bottom.toFloat())
                }

                if (newSizeKeep == null) {
                    var x = sizeKeep.left.toDouble()
                    var y = sizeKeep.top.toDouble()
                    val right = x + sizeKeep.width.toDouble()
                    val bottom = y + sizeKeep.height.toDouble()

                    newSizeKeep = Rect(x.toFloat(), y.toFloat(), right.toFloat(), bottom.toFloat())
                }

                // 绘制文本
                childNode.let { (_, attributedString) ->
                    attributedString.drawInRect(newSizeKeepText!!.toCGRect())
                }

                // 设置描边颜色和边框宽度
                CGContextSetStrokeColorWithColor(ctx, UIColor.blackColor.CGColor)
                CGContextSetLineWidth(ctx, 2.0)

                // 根据位置微调，避免上下左右叠加
                val adjustLeft = if (sizeKeep.left > 0f) -1.0 else 0.0 // 左边偏移，避免重叠
                val adjustTop = if (sizeKeep.top > 0f) -1.0 else 0.0   // 上边偏移，避免重叠
                val adjustRight = 0.0 // 保持右侧不变
                val adjustBottom = 0.0 // 保持底部不变

                // 计算调整后的 CGRect，避免中间边框重叠
                val adjustedRect = CGRectMake(
                    newSizeKeep!!.left.toDouble() + adjustLeft,
                    newSizeKeep!!.top.toDouble() + adjustTop,
                    newSizeKeep!!.width.toDouble() + adjustRight,
                    newSizeKeep!!.height.toDouble() + adjustBottom
                )

                // 绘制矩形的边框
                CGContextStrokeRect(ctx, adjustedRect)
            }
        } ?: throw IllegalArgumentException("context is null")
    }

}


@OptIn(ExperimentalObjCName::class)
@ObjCName("DTTableTextAttachment")
/**
 * TR
 */
class DTTableColumnTextAttachment(element: DTHTMLElement, options: Map<Any?, *>?) :
    DTImageTextAttachment(
        element,
        options
    ) {
    @Deprecated(
        "Use constructor instead",
        replaceWith = ReplaceWith("DTImageTextAttachment(contentData, ofType)"),
        level = DeprecationLevel.ERROR
    )
    override fun initWithData(contentData: NSData?, ofType: String?): DTImageTextAttachment {
        return this
    }

    var sizeKeep = Rect.Zero
    var offset = Offset.Zero

    internal val childNode = mutableListOf<DTTableRowTextAttachment>()
    fun addAll(rowAttachments: List<DTTableRowTextAttachment>?) {
        childNode.addAll(rowAttachments ?: return)
    }

    //    private val childNodes = mutableListOf<DTTableRowTextAttachment>()
    override fun stringByEncodingAsHTML(): String {
        return "<tr></tr>"
    }
    var index=0

    override fun drawInRect(rect: CValue<CGRect>, context: CGContextRef?) {
        // 确保 context 不为 null
        context?.let {
            childNode.forEachIndexed { index, attachment ->
                //这里是画 DTTableRowTextAttachment的
                val rowRect = (attachment as DTTableRowTextAttachment).sizeKeep
                val currIndex = this.index+index
                attachment.index = currIndex
                //这里是画 DTTableRowTextAttachment的
                attachment.drawInRect(
                    CGRectMake(
                        x = rowRect.left.toDouble(),
                        y = rowRect.top.toDouble(),
                        rowRect.width.toDouble(),
                        rowRect.height.toDouble(),
                    ), context
                )
            }
        } ?: throw IllegalArgumentException("context is null")
    }

    override fun displaySize(): CValue<CGSize> {
        return CGSizeMake(sizeKeep.width.toDouble(), sizeKeep.height.toDouble())
    }

    override fun originalSize(): CValue<CGSize> {
        return CGSizeMake(sizeKeep.width.toDouble(), sizeKeep.height.toDouble())
    }

    override fun description(): String {
        return "DTTableColumnTextAttachment@${hashCode()}"
    }
}


class TableHandler(private val maxWidth: Float, private val density: Density) :
    UoocTagHandler("table") {

    private val trHandler = TrHandler()
    private val tbodyHandler = object : UoocTagHandler("tbody"){
        override fun handleStartTag(currentTag: DTHTMLElement) {
            trHandler.handleStartTag(currentTag)
        }

        override fun handleEndTag(currentTag: DTHTMLElement) {
            trHandler.handleEndTag(currentTag)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun allHandlers(): List<TagHandlerProtocol> {
        return listOf(trHandler,tbodyHandler, this, *trHandler.childHandlers().toTypedArray())
    }

    private lateinit var tableAttachment: DTTableTextAttachment

    override fun handleStartTag(currentTag: DTHTMLElement) {
        tableAttachment = DTTableTextAttachment(maxWidth, currentTag, currentTag.attributes)
        currentTag.textAttachment = tableAttachment
    }
    @Suppress("UNCHECKED_CAST")
    override fun handleEndTag(currentTag: DTHTMLElement) {
        try {
            currentTag.setMargins(UIEdgeInsetsMake(10.0, 10.0, 10.0, 10.0))

            tableAttachment.addAll((trHandler.attachment() as? List<Any>) ?: emptyList())
            currentTag.displayStyle = DTHTMLElementDisplayStyleInline
            currentTag.setParagraphStyle(DTCoreTextParagraphStyle().apply {
                setAlignment(kCTTextAlignmentLeft)
                setParagraphSpacing(0.0)
                setParagraphSpacingBefore(0.0)
            })
            currentTag.preserveNewlines = true
            currentTag.size = CGSizeMake(tableAttachment.sizeKeep.width.toDouble()+20.0, tableAttachment.sizeKeep.height.toDouble()+20.0)
            currentTag.removeAllChildNodes()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    override fun description(): String {
        return "TableHandler@${hashCode()}"
    }

    override fun attachment(): Any {
        return tableAttachment
    }

}


private class TrHandler : UoocTagHandler("tr") {
    private val thHandler = ThHandler()
    private val tdHandler = TdHandler()

    fun childHandlers(): List<TagHandlerProtocol> {
        return listOf(thHandler, tdHandler)
    }

    private val attachments = mutableListOf<DTTableColumnTextAttachment>()
    private val map = mutableMapOf<DTHTMLElement, DTTableColumnTextAttachment>()
    override fun handleStartTag(currentTag: DTHTMLElement) {
        if(currentTag.name=="tbody"){
            currentTag.paragraphStyle =null
            currentTag.textAttachment =null
            currentTag.preserveNewlines = false
            return
        }
        map[currentTag] = DTTableColumnTextAttachment(currentTag, currentTag.attributes)
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleEndTag(currentTag: DTHTMLElement) {
        currentTag.textAttachment = (map[currentTag] ?: return).apply {
            thHandler.attachment().let {
                addAll(it as List<DTTableRowTextAttachment>)
            }
            thHandler.clean()
            tdHandler.attachment().let {
                addAll(it as List<DTTableRowTextAttachment>)
            }
            tdHandler.clean()
            attachments.add(this)
        }
        currentTag.paragraphStyle =null
        currentTag.textAttachment =null
        currentTag.preserveNewlines = false
    }

    override fun description(): String {
        return "TrHandler@${hashCode()}"
    }

    override fun attachment(): Any {
        return attachments
    }
}

private class ThHandler : UoocTagHandler("th") {
    private val attachments = mutableListOf<DTTableRowTextAttachment>()
    private val map = mutableMapOf<DTHTMLElement, DTTableRowTextAttachment>()
    override fun handleStartTag(currentTag: DTHTMLElement) {
        map[currentTag] = DTTableRowTextAttachment("th", currentTag, currentTag.attributes)
    }

    override fun handleEndTag(currentTag: DTHTMLElement) {
        currentTag.textAttachment = (map[currentTag] ?: return).apply {
            val list = currentTag.childNodes?.filterIsInstance<DTTextHTMLElement>() ?: emptyList()
            list.forEach {
                this.add(it.attributedString())
            }
            attachments.add(this)
        }
        currentTag.paragraphStyle =null
        currentTag.textAttachment =null
        currentTag.removeAllChildNodes()
        currentTag.preserveNewlines = false
    }

    override fun description(): String {
        return "ThHandler@${hashCode()}"
    }

    override fun attachment(): Any {
        return attachments
    }

    fun clean() {
        attachments.clear()
    }
}

private class TdHandler : UoocTagHandler("td") {
    private val attachments = mutableListOf<DTTableRowTextAttachment>()
    private val map = mutableMapOf<DTHTMLElement, DTTableRowTextAttachment>()
    override fun handleStartTag(currentTag: DTHTMLElement) {
        map[currentTag] = DTTableRowTextAttachment("td", currentTag, currentTag.attributes)
    }

    override fun handleEndTag(currentTag: DTHTMLElement) {
        currentTag.textAttachment = (map[currentTag] ?: return).apply {
            val list = currentTag.childNodes?.filterIsInstance<DTTextHTMLElement>() ?: emptyList()
            list.forEach {
                this.add(it.attributedString())
            }
            attachments.add(this)
        }
        currentTag.paragraphStyle =null
        currentTag.textAttachment =null
        currentTag.removeAllChildNodes()
        currentTag.preserveNewlines = false
    }

    override fun description(): String {
        return "TdHandler@${hashCode()}"
    }

    override fun attachment(): Any {
        return attachments
    }

    fun clean() {
        attachments.clear()
    }
}
