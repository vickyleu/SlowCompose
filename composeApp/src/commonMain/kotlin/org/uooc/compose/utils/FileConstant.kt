package org.uooc.compose.utils

import androidx.compose.ui.graphics.Color

object FileConstant {


    val PHOTO_EXTENSIONS = arrayOf(
        ".jpg",
        ".png",
        ".jpeg",
        ".bmp",
        ".webp",
        ".heic",
        ".heif",
        ".apng",
        ".avif",
        ".gif",
        ".tiff",
        ".tif",
        ".svg"
    )
    val VIDEO_EXTENSIONS = arrayOf(".mp4", ".mkv", ".webm", ".avi", ".3gp", ".mov", ".m4v", ".3gpp")
    val PARTIAL_SUPPORT_VIDEO_EXTENSIONS = arrayOf(".avi")
    val AUDIO_EXTENSIONS = arrayOf(".mp3", ".wav", ".wma", ".ogg", ".m4a", ".opus", ".flac", ".aac")
    val RAW_EXTENSIONS = arrayOf(".dng", ".orf", ".nef", ".arw", ".rw2", ".cr2", ".cr3")
    val SUPPORTING_EXIF_EXTENSIONS = arrayOf(".jpg", ".jpeg", ".png", ".webp", ".dng")

    enum class Extension {
        PHOTO, VIDEO, AUDIO, WORD, EXCEL, PDF, PPT, EXE, ZIP, OTHER
    }

    //  1. 通过文件后缀名判断文件类型,然后生成枚举类型
    fun getExtensionType(extension: String): Extension {
        return when {
            PHOTO_EXTENSIONS.contains(extension) -> Extension.PHOTO
            VIDEO_EXTENSIONS.contains(extension) -> Extension.VIDEO
            AUDIO_EXTENSIONS.contains(extension) -> Extension.AUDIO
            extension == ".doc" || extension == ".docx" -> Extension.WORD
            extension == ".xls" || extension == ".xlsx" -> Extension.EXCEL
            extension == ".pdf" -> Extension.PDF
            listOf(".ppt", ".pptx").contains(extension) -> Extension.PPT
            listOf(".exe", ".dmg").contains(extension) -> Extension.EXE
            listOf(".zip", ".7z", ".rar").contains(extension) -> Extension.ZIP
            else -> Extension.OTHER
        }
    }

    // 通过枚举类型生成首字母和颜色Color
    fun getExtensionColor(extension: String): Pair<String, Color> {
        getExtensionType(extension).let {
            return getExtensionColor(it)
        }
    }

    fun getColorForChar(firstChar: Char): Color {
        val uniqueChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val index = uniqueChars.indexOf(firstChar.uppercaseChar())
        val hueStep = 360 / uniqueChars.length
        val hue = (index * hueStep) % 360 // 确保每个字符生成的颜色是均匀分布的
        val saturation = 0.7f // 适中的饱和度，避免白色
        val value = 0.8f // 适中的亮度，避免黑色
        return Color.hsv(hue.toFloat(), saturation, value) // 生成颜色
    }

    fun getExtensionTypeChar(extension: Extension): String {
        return when (extension) {
            Extension.PPT -> "P"
            Extension.WORD -> "W"
            Extension.PHOTO -> "P"
            Extension.VIDEO -> "V"
            Extension.AUDIO -> "A"
            Extension.EXCEL -> "E"
            Extension.PDF -> "P"
            Extension.EXE -> "E"
            Extension.ZIP -> "Z"
            Extension.OTHER -> "O"
        }
    }

    fun getExtensionColor(extension: Extension): Pair<String, Color> {
        return when (extension) {
            Extension.PPT -> Pair("P", Color(0xFFFF9333))
            Extension.WORD -> Pair("W", Color(0xFF4876F9))

            Extension.PHOTO -> Pair("P", Color(0xFF4CAF50))
            Extension.VIDEO -> Pair("V", Color(0xFF2196F3))
            Extension.AUDIO -> Pair("A", Color(0xFF9C27B0))
            Extension.EXCEL -> Pair("E", Color(0xFF673AB7))
            Extension.PDF -> Pair("P", Color(0xFF3F51B5))
            Extension.EXE -> Pair("E", Color(0xFF795548))
            Extension.ZIP -> Pair("Z", Color(0xFF607D8B))
            Extension.OTHER -> Pair("O", Color(0xFF9E9E9E))
        }
    }

}

// 扩展函数获取文件后缀
fun String.getFileExtension(): String {
    return this.substringAfterLast('.', "").lowercase()
}

// 将文件大小转换为可视化大小,B,KB,MB,GB,TB,PB
fun ByteArray.formatToVisualSize(): String {
    return this.size.toDouble().formatToVisualSize()
}


fun Double.formatToVisualSize(): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
    var displaySize = this
    var unitIndex = 0
    while (displaySize >= 1024 && unitIndex < units.size - 1) {
        displaySize /= 1024
        unitIndex++
    }
    return "%.2f %s".formatImpl(displaySize, units[unitIndex])
}

expect fun String.formatImpl(vararg args: Any): String
