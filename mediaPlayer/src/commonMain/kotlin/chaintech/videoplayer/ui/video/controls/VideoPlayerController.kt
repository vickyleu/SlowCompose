package chaintech.videoplayer.ui.video.controls

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import chaintech.videoplayer.model.PlayerConfig
import chaintech.videoplayer.model.PlayerSpeed
import chaintech.videoplayer.ui.video.VideoSource
import com.github.jing332.filepicker.base.FileImpl

class VideoPlayerController(videoSource:VideoSource, val playerConfig: PlayerConfig = PlayerConfig()) {
    val source = mutableStateOf<VideoSource>(videoSource)
    val file = mutableStateOf(FileImpl(""))

    fun changeSource(value: VideoSource) {
        if (when (value) {
                is VideoSource.File -> value.file.getAbsolutePath().isNotEmpty()
                is VideoSource.Url -> value.url.isNotEmpty()
            }
        ) {
            if (source.value == value) {
                _forceUpdate.value = true
            }
        }
        seekPos.value = 0F
        source.value = value
        _forceUpdate.value = false
    }

    val seekPos = mutableStateOf(0F)

    private val _forceUpdate = mutableStateOf(false)

    val forceUpdate: State<Boolean>
        get() = _forceUpdate

    val extra = mutableStateOf<Any>(source.value)
    val isPause = mutableStateOf(playerConfig.autoPlay.not())  // State for pausing/resuming video
    val showControls = mutableStateOf(true)  // State for showing/hiding controls
    val isFullScreen = mutableStateOf(false)
    val isMute = mutableStateOf(false)
    val selectedSpeed = mutableStateOf(PlayerSpeed.X1) // Selected playback speed
    val isScreenLocked = mutableStateOf(false)

    val currentPlayingTime = mutableStateOf(0L)
    val duration = mutableStateOf(0L)

}