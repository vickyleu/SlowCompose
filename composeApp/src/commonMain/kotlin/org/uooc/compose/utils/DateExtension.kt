package org.uooc.compose.utils

import io.wojciechosak.calendar.config.DayState
import io.wojciechosak.calendar.config.MonthYear
import io.wojciechosak.calendar.utils.today
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Clock.System.nowLocalDateTime(): LocalDateTime {
    return Instant.fromEpochMilliseconds(this.now().toEpochMilliseconds())
        .toLocalDateTime(TimeZone.currentSystemDefault())
}

operator fun LocalDateTime.minus(other: LocalDateTime): Duration {
    return ((this.toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()) - (other.toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()))
        .toDuration(DurationUnit.MILLISECONDS)
}

// 将数字转换为中文大写
fun Int.toChineseCapital(): String {
    val chineseNum = arrayOf("零", "一", "二", "三", "四", "五", "六", "七", "八", "九")
    val chineseUnit = arrayOf("", "十", "百", "千", "万", "亿")
    val numStr = this.toString()
    val numLen = numStr.length
    var result = ""
    for (i in 0 until numLen) {
        val num = numStr[i].toString().toInt()
        val unit = chineseUnit[numLen - i - 1]
        result += chineseNum[num] + unit
    }
    return result
}

fun Int.toChineseWeekCapital(): String {
    if (this <= 0 || this > 7) return ""
    val chineseNum = arrayOf("", "一", "二", "三", "四", "五", "六", "日")
    return chineseNum[this]
}

// 月份转换为中文大写,需要跳过0
fun Int.toChineseMonthCapital(): String {
    return if (this < 1 || this > 12) {
        ""
    } else {
        (this).toChineseCapital()
    }
}

// 数字月份数字补全为两位
fun Int.toMonthString(): String {
    return if (this < 10) "0$this" else this.toString()
}

// 数字补0
fun Int.padding(): String {
    return if (this <= 0) {
        "00"
    } else {
        if (this < 10) "0$this" else this.toString()
    }
}

fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
}

fun Long.toMinuteDuration(unit: DurationUnit = DurationUnit.MINUTES): Duration {
    return this.toDuration(unit)
}

fun String.toSafeLocalDateTimeString(originFormat: String, format: String): String {
    return toSafeLocalDateTime(originFormat).format(format)
}

fun tagForDate(): String {
    return Clock.System.nowLocalDateTime().format("HH:mm:ss:SSS")
}

fun String.toSafeLocalDateTime(format: String): LocalDateTime {
    try {
        return this.toLongOrNull().let {
            if (it != null) {//如果是时间戳字符串
                return@let Instant.fromEpochMilliseconds(this.toLong() * 1000)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            }
            //如果是时间字符串
           return@let this.formatDateTime(format).div(1000).toString().toSafeLocalDateTime(format)
        }
    } catch (e: Exception) {
        try {
            return this.formatDateTime(format).div(1000).toString().toSafeLocalDateTime(format)
        }catch (e:Exception){
            e.printStackTrace()
            return Clock.System.nowLocalDateTime()
        }
    }
}


expect fun String.formatDateTime(pattern: String): Long

fun Long.toLocalDate(): LocalDate {
    return toLocalDateTime().date
}

fun Int.toLocalDate(): LocalDate {
    return toLong().toLocalDate()
}

expect fun LocalDateTime.format(pattern: String): String

val MonthYear.lengthOfMonth: Int
    get() {
        return when (this.month.number) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (this.year % 4 == 0) 29 else 28
            else -> throw IllegalArgumentException("Invalid month")
        }
    }

val DayState.isToday: Boolean
    get() = date == LocalDate.today()