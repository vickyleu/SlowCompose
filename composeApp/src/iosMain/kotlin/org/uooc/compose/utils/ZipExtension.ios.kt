package org.uooc.compose.utils


import com.github.jing332.filepicker.base.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.getBytes
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.zlib.Z_OK
import platform.zlib.compress

actual fun ByteArray.compressToSize(targetSize: Long, isImage: Boolean): ByteArray {
    return if (isImage) {
        compressImageToSize(targetSize)
    } else {
        compressDataToSize(targetSize)
    }
}

private fun ByteArray.compressImageToSize(targetSize: Long): ByteArray {
    if(this.isEmpty()){
        throw IllegalArgumentException("图片是空的")
    }
    if(this.size <= targetSize){
        return this
    }
    val uiImage = UIImage(data = this.toNSData())
    var quality = 1.0
    var compressedData: NSData
    do {
        quality -= 0.01
        compressedData = UIImageJPEGRepresentation(uiImage, quality) ?: return this
    } while (compressedData.length.toInt() > targetSize && quality > 0.0)
    return compressedData.toByteArray()
}

private fun ByteArray.compressDataToSize(targetSize: Long): ByteArray {
    if(this.isEmpty()){
        throw IllegalArgumentException("数据是空的")
    }
    if(this.size <= targetSize){
        return this
    }
    val bufferSize = targetSize.toInt()
    val compressedBuffer = ByteArray(bufferSize)
    // 使用 ULongVar 和 UByteVar 数组来存储目标大小和压缩数据
    val compressedSize = nativeHeap.alloc<ULongVar>()
    compressedSize.value = compressedBuffer.size.toULong()
    val result = this.usePinned { sourcePinned ->
        compressedBuffer.usePinned { destPinned ->
            compress(
                destPinned.addressOf(0).reinterpret(),
                compressedSize.ptr,
                sourcePinned.addressOf(0).reinterpret(),
                this.size.toULong()
            )
        }
    }
    return if (result == Z_OK) {
        compressedBuffer.copyOfRange(0, compressedSize.value.toInt())
    } else {
        this
    }
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    memScoped {
        val buffer = ByteArray(this@toByteArray.length.toInt())
        buffer.usePinned {
            this@toByteArray.getBytes(it.addressOf(0))
            return buffer
        }
    }
}