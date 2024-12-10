package org.uooc.compose.utils

class CleanedException(message: String, cause: Throwable) : Exception(message, cause) {
    override fun toString(): String {
        val stackTrace = cause?.stackTraceToString() ?: ""
        val filteredStack = stackTrace.lineSequence()
            .filter { it.contains("androidx.compose") } // 过滤出Compose相关的异常
            .take(5) // 只取前5行，避免太长
            .joinToString("\n")
        return if (filteredStack.isNotEmpty()) {
            "CleanedException: ${cause?.message ?: "Unknown error"}\n$filteredStack"
        } else {
            "CleanedException: ${cause?.message ?: "Unknown error"}"
        }
    }
}

fun Throwable.toCleanedException(): CleanedException {
    return CleanedException("Filtered exception", this)
}