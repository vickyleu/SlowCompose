package org.uooc.compose.utils

import kotlin.math.round

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun <K, V> Map<K, V>.removeKey(key: K): Map<K, V> {
    return  HashMap(this.filterNot { it.key == key })
}

fun Float.roundToIntOrNon():String{
    return if (this % 1 == 0f) {
        this.toInt().toString()
    } else {
        this.toString()
    }
}