package chaintech.videoplayer.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

expect fun formatMinSec(value: Int): String

expect fun formatInterval(value: Int): Int

@Composable
expect fun LandscapeOrientation(
    isLandscape: MutableState<Boolean>,
    content: @Composable () -> Unit
)