package org.uooc.compose.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

actual fun DialogPropertiesImpl(
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    usePlatformDefaultWidth: Boolean,
    useTransparentColor: Boolean
): DialogProperties {
    return DialogProperties(
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        usePlatformDefaultWidth = usePlatformDefaultWidth,
        usePlatformInsets = false,
        useSoftwareKeyboardInset = false,
        scrimColor = if (useTransparentColor) Color.Transparent else Color.Black.copy(alpha = 0.6f),
    )
}


actual fun PopupPropertiesImpl(
    focusable: Boolean,
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    usePlatformDefaultWidth: Boolean,
    clippingEnabled: Boolean,
    useTransparentColor: Boolean
): PopupProperties {
    return PopupProperties(
        focusable = focusable,
        clippingEnabled = clippingEnabled,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        usePlatformDefaultWidth = usePlatformDefaultWidth,
        usePlatformInsets = false,
    )
}

@Composable
actual fun PaddinglessPopup(
    alignment: Alignment,
    offset: IntOffset,
    onDismissRequest: () -> Unit,
    properties: PopupProperties,
    content: @Composable () -> Unit
) {
    Popup(
        alignment = alignment,
        offset = offset,
        onDismissRequest = onDismissRequest,
        properties=PopupProperties(
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = properties.dismissOnClickOutside,
            usePlatformDefaultWidth = properties.usePlatformDefaultWidth,
            usePlatformInsets = properties.usePlatformInsets,
            clippingEnabled = properties.clippingEnabled,
            focusable = properties.focusable,
        )
    ) {
        content.invoke()
    }
}



@Composable
actual fun TransparentDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        DialogProperties(
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = properties.dismissOnClickOutside,
            usePlatformDefaultWidth = properties.usePlatformDefaultWidth,
            usePlatformInsets = properties.usePlatformInsets,
            useSoftwareKeyboardInset = properties.useSoftwareKeyboardInset,
            scrimColor = properties.scrimColor
        )
    ) {
        content.invoke()
    }
}

@OptIn(InternalComposeUiApi::class)
@Composable
actual fun PaddinglessDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        DialogProperties(
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = properties.dismissOnClickOutside,
            usePlatformDefaultWidth = properties.usePlatformDefaultWidth,
            usePlatformInsets = false,//properties.usePlatformInsets,
            useSoftwareKeyboardInset = false,////properties.useSoftwareKeyboardInset,
            scrimColor = Color.Black.copy(alpha = 0.6f),
        )
    ) {
        content.invoke()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
actual fun painterResourceMtf(resource: DrawableResource): Painter {
    val resourcePainter = painterResource(resource = resource)
//       when(resourcePainter){
//              is BitmapPainter -> {
//                  resourcePainter.hashCode()
//                  val image=imageResource(resource)
//                  println("${image.asSkiaBitmap().bounds}image width: ${image.width}, height: ${image.height}")
//                return BitmapPainter(image, srcSize = IntSize(10, 10))
//              }
//              is VectorPainter -> {
//                return resourcePainter
//              }
//              else -> {
//                return resourcePainter
//              }
//       }

    return resourcePainter
}