package org.uooc.compose.utils

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.uooc.compose.base.BuildConfigImpl
//import org.uooc.compose.generated.resource
import org.uooc.compose.resources.Res

@OptIn(ExperimentalResourceApi::class)
internal suspend fun Res.file(path: String): ByteArray {
    return readBytes("files/$path")
}


@Composable
expect fun painterResourceMtf(resource: DrawableResource): Painter


@Composable
internal fun Modifier.rippleClickable(
    enabled: Boolean = true,
    ripple: Boolean = BuildConfigImpl.isDebug,//去掉涟漪效果, 生产环境不显示, 因为设计不喜欢
    debounceTime: Long = 1000L, // 设置防抖时间
    onClick: () -> Unit
) = composed(
    factory = {
        val interactionSource = remember { MutableInteractionSource() }
        val rippleIndication = LocalIndication.current
        var lastClickTime by remember { mutableStateOf(0L) }
        this then clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = if(ripple) rippleIndication else null,
        ) {
            val currentTime = Clock.System.now().toEpochMilliseconds()
            if (currentTime - lastClickTime >= debounceTime && enabled) {
                lastClickTime = currentTime
                onClick()
            }
        }
    }
)


internal suspend fun Res.lottieFilePath(name: String): String {
    return file("files/$name.json").let {
        it.contentToString()
    }
}

//internal fun Res.lottie(name: String): String {
//    return resource("files/$name.json")
//}

@OptIn(ExperimentalResourceApi::class)
internal suspend fun Res.lottieBytes(name: String): ByteArray {
    return readBytes("files/$name.json")
}


@Composable
expect fun TransparentDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogPropertiesImpl(),
    content: @Composable () -> Unit
)


@Composable
expect fun PaddinglessDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogPropertiesImpl(),
    content: @Composable () -> Unit
)


@Composable
expect fun PaddinglessPopup(
    alignment: Alignment = Alignment.BottomCenter,
    offset: IntOffset,
    onDismissRequest: () -> Unit,
    properties: PopupProperties,
    content: @Composable () -> Unit
)

expect fun DialogPropertiesImpl(
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    usePlatformDefaultWidth: Boolean = true,
    useTransparentColor: Boolean = false
): DialogProperties

expect fun PopupPropertiesImpl(
    focusable: Boolean = false,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    usePlatformDefaultWidth: Boolean = true,
    clippingEnabled: Boolean = true,
    useTransparentColor: Boolean = false
): PopupProperties

//@Composable
//expect fun ripple(
//    bounded: Boolean = true,
//    radius: Dp = Dp.Unspecified,
//    color: Color = Color.Unspecified
//): Indication


//@Composable
//fun Modifier.drawShadow(
//    shadowColor: Color,
//    blurRadius: Dp,
//    offset: DpOffset
//): Modifier = this.then(
//    with(LocalDensity.current) {
//        Modifier.drawBehind {
//            val transformOffset = Offset(offset.x.toPx(), offset.y.toPx())
//            withTransform({
//                translate(left = transformOffset.x, top = transformOffset.y)
//            }) {
//                drawRect(
//                    color = shadowColor,
//                    size = size
//                )
//            }
//        }
//            .blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
//    }
//)


fun lightenColor(color: Color, amount: Float): Color {
    // 提取颜色的RGBA值
    val r = (color.red * 255 + amount * 255).coerceIn(0f, 255f)
    val g = (color.green * 255 + amount * 255).coerceIn(0f, 255f)
    val b = (color.blue * 255 + amount * 255).coerceIn(0f, 255f)
    val alpha = color.alpha

    // 返回一个新的颜色
    return Color(r / 255, g / 255, b / 255, alpha)
}

@Composable
fun Modifier.drawCard(
    rect: Rect,
    borderWidth: Dp = 0.dp,
    roundCorner: Dp,
    alpha: Float = 1.0f,
    brush: ((Rect) -> Brush)? = null,
    colorBackground: ((Rect) -> Color)? = null,
    brush2: ((Rect) -> Brush)? = null,
): Modifier {

    return this

}

@OptIn(InternalVoyagerApi::class)
val LocalNavigatorController = compositionLocalOf(structuralEqualityPolicy<Navigator>()) {
    Navigator(
        screens = listOf(), key = "fakeKey", stateHolder = object : SaveableStateHolder {
            @Composable
            override fun SaveableStateProvider(key: Any, content: @Composable () -> Unit) {
            }

            override fun removeState(key: Any) {
            }
        },
        disposeBehavior = NavigatorDisposeBehavior()
    )
}

internal val FakeScreen = object : Screen {
    @Composable
    override fun Content() {
    }
}

val LocalTransitioningNotify = compositionLocalOf(structuralEqualityPolicy()) {
    mutableStateOf(false to FakeScreen)
}

//val LocalNavigatorController = compositionLocalOf(structuralEqualityPolicy<NavHostController?>()) {
//    null
//}

