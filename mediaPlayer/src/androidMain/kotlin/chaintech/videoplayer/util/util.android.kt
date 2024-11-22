package chaintech.videoplayer.util

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import java.util.concurrent.TimeUnit

actual fun formatMinSec(value: Int): String {
    return String.format(
        Locale.CHINA, "%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(value.toLong()),
        TimeUnit.MILLISECONDS.toSeconds(value.toLong()) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(value.toLong())
        )
    )
}

actual fun formatInterval(value: Int): Int {
    return value * 1000
}

@Composable
actual fun LandscapeOrientation(
    isLandscape: MutableState<Boolean>,
    content: @Composable () -> Unit
) {
    val activity = LocalContext.current as? Activity
    DisposableEffect(isLandscape.value) {
        activity?.apply {
            if (isLandscape.value) {
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        )
                requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            else {
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        )
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
        onDispose {
            activity?.apply {
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        )
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }
    content.invoke()
}