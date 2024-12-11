package org.uooc.compose.utils

import com.fleeksoft.ksoup.engine.KsoupEngineImpl
import com.fleeksoft.ksoup.io.Charset
import com.fleeksoft.ksoup.ported.toByteArray
import com.github.jing332.filepicker.base.toNSData
import platform.Foundation.NSASCIIStringEncoding
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSData
import platform.Foundation.NSISOLatin1StringEncoding
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.create
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.stringByRemovingPercentEncoding

actual fun String.urlDecode(): String {
    return NSString.create(string = this).stringByRemovingPercentEncoding ?: this
}

actual fun String.urlEncode(): String {
    val allowedCharacterSet = NSCharacterSet.URLQueryAllowedCharacterSet
    return NSString.create(string = this).stringByAddingPercentEncodingWithAllowedCharacters(allowedCharacterSet) ?: this
}


actual fun String.fixCorruptedString(): String {
    // 尝试用给定字符集进行解码
    fun tryDecode(data: NSData, encoding: ULong): String? {
        return NSString.create(data = data, encoding = encoding)?.toString()
    }

    // 比较重新编码后的字节数组与原始字节数组是否一致
    fun isEncodingCorrect(
        originalData: ByteArray,
        decodedString: String,
        encoding: Charset
    ): Boolean {
        return decodedString.toByteArray(encoding).contentEquals(originalData)
    }

    // 如果当前字符串没有乱码，直接返回
    if (!this.isNameCorrupted()) {
        return this
    }

    // 原始的字节数组
    val originalData = this.encodeToByteArray()
    val data = originalData.toNSData()

    val encodingsToTry: List<Pair<ULong, Charset>> = listOf(
        NSISOLatin1StringEncoding to KsoupEngineImpl.charsetForName("ISO-8859-1"),  // ISO-8859-1 (西欧编码)
        NSUTF8StringEncoding to KsoupEngineImpl.charsetForName("UTF-8"),            // UTF-8
        NSASCIIStringEncoding to KsoupEngineImpl.charsetForName("US-ASCII"),        // ASCII 编码
//        0x80000400UL to KsoupEngineImpl.charsetForName("Windows-1252")    // Windows-1252 (仅在平台支持时)
    )

    // 遍历编码列表，尝试修复
    for ((encoding, charset) in encodingsToTry) {
        val decodedString = tryDecode(data, encoding) ?: continue
        // 验证解码是否正确
        if (isEncodingCorrect(originalData, decodedString, charset)) {
            return decodedString // 如果重新编码一致，返回修复后的字符串
        }
    }

    // 如果所有尝试都失败，返回原始字符串
    return this
}