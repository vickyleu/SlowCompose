package org.uooc.compose.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import coil3.*
import coil3.decode.*
import coil3.fetch.*
import coil3.request.Options
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import platform.AVFoundation.*
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.*
import platform.CoreMedia.*
import platform.Foundation.*
import platform.UIKit.*

class VideoFrameDecoder(
    private val source: SourceFetchResult,
    private val timeInSeconds: Double = 0.0
) : Decoder {

    override suspend fun decode() = withContext(Dispatchers.Main) {
        try {
            val filePath = source.source.file().toString()
            println("VideoFrameDecoder:filePath====>> $filePath")
            val image = decodeFrame(filePath, timeInSeconds)
            image?.let {
                val coilImage = it.toCoilImage()
                DecodeResult(
                    image = coilImage,
                    isSampled = true
                )
            }
                ?: throw IllegalArgumentException("Unable to decode video frame.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Video frame decoding error: ${e.message}", e)
        }
    }


    private fun decodeFrame(filePath: String, timeInSeconds: Double): UIImage? {
        val url = NSURL.fileURLWithPath(filePath)
        val asset = AVAsset.assetWithURL(url)
        val imageGenerator = AVAssetImageGenerator(asset).apply {
            appliesPreferredTrackTransform = true
            requestedTimeToleranceBefore = kCMTimeZero.readValue()
            requestedTimeToleranceAfter = kCMTimeZero.readValue()
        }
        val time = CMTimeMakeWithSeconds(timeInSeconds, preferredTimescale = 600)
        return try {
            memScoped {
                val actualTime = alloc<CMTime>()
                val cgImage = imageGenerator.copyCGImageAtTime(time, actualTime.ptr, null)
                UIImage.imageWithCGImage(cgImage)
            }
        } catch (e: Exception) {
            println("Error decoding video frame: ${e.message}")
            null
        }
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            return if (isApplicable(result.mimeType)) {
                VideoFrameDecoder(result)
            } else null
        }

        private fun isApplicable(mimeType: String?): Boolean {
            return mimeType == "video/mp4" || mimeType == "video/quicktime"
        }
    }
}


private fun UIImage.toCoilImage(): Image {
    return toImageBitmap().asSkiaBitmap().asImage()
}
private fun UIImage.toImageBitmap(): ImageBitmap {
    val skiaImage = this.toSkiaImage() ?: return ImageBitmap(1, 1)
    return skiaImage.toComposeImageBitmap()
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toSkiaImage(): org.jetbrains.skia.Image? {
    val imageRef = this.CGImage ?: return null

    val width = CGImageGetWidth(imageRef).toInt()
    val height = CGImageGetHeight(imageRef).toInt()

    val bytesPerRow = CGImageGetBytesPerRow(imageRef)
    val data = CGDataProviderCopyData(CGImageGetDataProvider(imageRef))
    val bytePointer = CFDataGetBytePtr(data)
    val length = CFDataGetLength(data)

    val alphaType = when (CGImageGetAlphaInfo(imageRef)) {
        CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst,
        CGImageAlphaInfo.kCGImageAlphaPremultipliedLast -> ColorAlphaType.PREMUL

        CGImageAlphaInfo.kCGImageAlphaFirst,
        CGImageAlphaInfo.kCGImageAlphaLast -> ColorAlphaType.UNPREMUL

        CGImageAlphaInfo.kCGImageAlphaNone,
        CGImageAlphaInfo.kCGImageAlphaNoneSkipFirst,
        CGImageAlphaInfo.kCGImageAlphaNoneSkipLast -> ColorAlphaType.OPAQUE

        else -> ColorAlphaType.UNKNOWN
    }

    val byteArray = ByteArray(length.toInt()) { index ->
        bytePointer!![index].toByte()
    }

    CFRelease(data)
    CGImageRelease(imageRef)

    val skiaColorSpace = ColorSpace.sRGB
    val colorType = ColorType.RGBA_8888

    // Convert RGBA to BGRA
    for (i in byteArray.indices step 4) {
        val r = byteArray[i]
        val g = byteArray[i + 1]
        val b = byteArray[i + 2]
        val a = byteArray[i + 3]

        byteArray[i] = b
        byteArray[i + 2] = r
    }

    return org.jetbrains.skia.Image.makeRaster(
        imageInfo = ImageInfo(
            width = width,
            height = height,
            colorType = colorType,
            alphaType = alphaType,
            colorSpace = skiaColorSpace
        ),
        bytes = byteArray,
        rowBytes = bytesPerRow.toInt(),
    )
}
