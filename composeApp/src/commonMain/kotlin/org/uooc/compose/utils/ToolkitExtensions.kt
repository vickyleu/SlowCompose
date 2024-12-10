package org.uooc.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import coil3.PlatformContext

fun String.toFormatCountString():String{
    return this.toIntOrNull()?.toFormatCountString()?:"0"
}

expect fun isKeyboardVisible(platformContext: PlatformContext): Boolean

/**
 * 使用curl将日志发送到charles
 */
expect fun printlnFix(message: String)


infix fun <A, B, C> Pair<A, B>.to(third: C): Triple<A, B, C> {
    return Triple(this.first, this.second, third)
}

fun Int.formatToVisualSize():String{
    return this.toDouble().formatToVisualSize()
}

fun Long.formatToVisualSize():String{
    return this.toDouble().formatToVisualSize()
}

fun Int.toFormatCountString():String{
    val count = this
    return when {
        count <= 0 -> {
            "0"
        }
        count in 1..99999 -> {
            "$count"
        }
        count in 100000..990000 -> {
            "${count / 10000}w"
        }
        else -> {
            "99w+"
        }
    }
}

expect fun killApp(context: PlatformContext)