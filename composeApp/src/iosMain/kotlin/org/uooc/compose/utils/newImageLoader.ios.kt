package org.uooc.compose.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.request.maxBitmapSize
import coil3.size.Size
import coil3.util.DebugLogger
import coil3.util.Logger
import kotlinx.coroutines.CoroutineScope
import okio.FileSystem
import okio.Path.Companion.toPath
import org.jetbrains.skia.Image
import kotlin.coroutines.CoroutineContext
actual fun newImageLoader(context: PlatformContext,
                          screenSize:Size,
                          coroutineContext:CoroutineContext): ImageLoader {
    val memoryPercent = if (isLowMemoryDevice())
        0.25 else 0.75

    return ImageLoader.Builder(context)
        .components {
            add(VideoFrameDecoder.Factory())
//            add(ImageDecoderDecoder.Factory())
        }
        /*.components {
//            add(SvgDecoder.Factory(true))
//            add(AnimatedImageDecoder.Factory())
//            add(ThumbnailDecoder.Factory())
        }*/
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, percent = memoryPercent)
                .build()
        }

        .diskCache {
            DiskCache.Builder()
                .directory(getCacheDirectory("image_cache").path!!.apply {
                    println("image_cache: $this")
                }.toPath())
                .maxSizePercent(1.0)
                .build()
        }
        .crossfade(100)
//        .coroutineContext(coroutineContext)
//        .fetcherCoroutineContext(coroutineContext)
//        .decoderCoroutineContext(coroutineContext)
        .maxBitmapSize(screenSize)
        .logger(DebugLogger(minLevel = Logger.Level.Error))
        .build()

}

//actual fun newKamelConfig(context: PlatformContext): KamelConfig {
//    return KamelConfig {
//        takeFrom(KamelConfig.Default)
//        svgDecoder()
//        imageBitmapDecoder()
//        imageVectorDecoder()
//        animatedImageDecoder()
//    }
//}

actual fun ComposeImageBitmap(byteArray: ByteArray): ImageBitmap {
    return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
}