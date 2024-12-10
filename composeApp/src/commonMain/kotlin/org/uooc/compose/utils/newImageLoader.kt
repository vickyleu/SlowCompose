package org.uooc.compose.utils

import androidx.compose.ui.graphics.ImageBitmap
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.size.Size
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

expect fun newImageLoader(context: PlatformContext,
                          screenSize: Size,
                          coroutineContext: CoroutineContext): ImageLoader


expect fun ComposeImageBitmap(byteArray: ByteArray):ImageBitmap