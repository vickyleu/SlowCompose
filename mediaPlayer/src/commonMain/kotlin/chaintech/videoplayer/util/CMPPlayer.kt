package chaintech.videoplayer.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import chaintech.videoplayer.model.PlayerSpeed
import chaintech.videoplayer.ui.video.VideoSource

@Composable
expect fun CMPPlayerCompose(
    modifier: Modifier,
    source: State<VideoSource>,
    forceUpdate: State<Boolean>,
    isPause: MutableState<Boolean>,
    isMute: State<Boolean>,
    isLooping: Boolean,
    totalTime: MutableState<Int>,
    seekPos: MutableState<Float>,
    currentTime: ((Int) -> Unit),
    onAutomaticallyPause: () -> Unit,
    isSliding: MutableState<Boolean>,
    sliderTime: MutableState<Int?>,
    speed: MutableState<PlayerSpeed>,
    fullscreen: MutableState<Boolean>
):CMPPlayer


expect class CMPPlayer


expect fun CMPPlayer.getCurrentFirstFrame(size: Size): ImageBitmap?
expect fun CMPPlayer.seekTo(position: Long)
expect fun CMPPlayer.release()