package org.uooc.compose.utils

fun String.getMatchesValue(regex: Regex, index: Int): String? {
    val rlt = regex.find(this)
    return rlt?.groupValues?.mapIndexedNotNull { i, s ->
        println("base64String:::::>>>>  s====$s  i====$i")
        if (i == index) {
            s
        } else null
    }?.firstOrNull()
}
fun String.isNameCorrupted(): Boolean {
    // 文件名中的非法字符（可以根据系统实际情况调整）
    val illegalCharacters = listOf(
        '\uFFFD',  // 替换字符 (�) 表示无法显示的字符
        '\u0000',  // 空字符
        '/', '\\', '?', '%', '*', ':', '|', '"', '<', '>', // 常见的非法字符
    )

    // 检查是否包含非法字符
    if (this.any { it in illegalCharacters }) {
        return true
    }

    // 进一步检测是否有无法显示的控制字符（ASCII 范围的控制字符通常在0x00到0x1F）
    if (this.any { it.isISOControl() }) {
        return true
    }

    return false
}
// 扩展函数检测字符是否为 ISO 控制字符
fun Char.isISOControl(): Boolean {
    return this.code in 0x00..0x1F || this.code in 0x7F..0x9F
}
// 尝试修复乱码文件名
expect fun String.fixCorruptedString(): String


fun String.safeString(): String {
    return this
        .replace("\\", "\\\\") // 转义反斜杠
        .replace("\"", "\\\"") // 转义双引号
        .replace("\n", "\\n")  // 转义换行符
        .replace("\r", "\\r")  // 转义回车符
}


expect fun String.urlDecode(): String
expect fun String.urlEncode(): String