package org.uooc.compose.utils

import kotlinx.datetime.LocalDateTime
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.timeIntervalSince1970

actual fun LocalDateTime.format(pattern: String): String {
    val dateFormatter = NSDateFormatter().apply {
        dateFormat = pattern
    }
    val nsDate = NSCalendar.currentCalendar.dateFromComponents(
        NSDateComponents().apply {
            year = this@format.year.toLong()
            month = this@format.monthNumber.toLong()
            day = this@format.dayOfMonth.toLong()
            hour = this@format.hour.toLong()
            minute = this@format.minute.toLong()
            second = this@format.second.toLong()
        }
    )
    return dateFormatter.stringFromDate(nsDate ?: NSDate())
}

actual fun String.formatDateTime(pattern: String): Long {
    val dateFormatter = NSDateFormatter().apply {
        dateFormat = pattern
    }
    val nsDate = dateFormatter.dateFromString(this) ?: throw IllegalArgumentException("Invalid date format or pattern")
    return (nsDate.timeIntervalSince1970 * 1000).toLong() // 转换为毫秒
}