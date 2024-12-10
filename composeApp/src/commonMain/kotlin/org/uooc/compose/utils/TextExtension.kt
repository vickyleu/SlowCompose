package org.uooc.compose.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // 仅保留数字字符
        val digits = text.text.filter { it.isDigit() }
        // 限制长度为11
        val trimmed = if (digits.length > 11) digits.substring(0..10) else digits
        // 格式化为大陆手机号码格式
        val formatted = buildString {
            for (i in trimmed.indices) {
                append(trimmed[i])
                if (i == 2 || i == 6) append(" ")
            }
        }

        // 创建原始到转换后的偏移映射
        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 6 -> offset + 1
                    offset <= 10 -> offset + 2
                    else -> formatted.length
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 3 -> offset
                    offset <= 8 -> offset - 1
                    offset <= 12 -> offset - 2
                    else -> trimmed.length
                }
            }
        }
        return TransformedText(AnnotatedString(formatted), numberOffsetTranslator)
    }
}