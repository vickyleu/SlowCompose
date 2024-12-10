package org.uooc.compose.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

val LocalToaster = compositionLocalOf(structuralEqualityPolicy()) {
    ToasterState(
        coroutineScope = CoroutineScope(
            SupervisorJob()
        )
    )
}

fun ToasterState.showToast(msg: String?, type: ToastType = ToastType.Toast) {
    val message = msg ?: return
    if (message.isEmpty()) return
    show(
        message = message,
//        id=ToastType.Toast.ordinal,
        type = type//[Normal | Toast | Success | Info | Warning | Error],
    )
}