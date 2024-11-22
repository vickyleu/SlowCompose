package chaintech.videoplayer.ui.reel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import chaintech.videoplayer.model.PlayerConfig
import chaintech.videoplayer.ui.video.ComponentBuilder
import chaintech.videoplayer.ui.video.PlayButtonBuilder
import chaintech.videoplayer.ui.video.ProgressBuilder
import chaintech.videoplayer.ui.video.VideoPlayerWithControl
import chaintech.videoplayer.ui.video.VideoSource
import chaintech.videoplayer.ui.video.controls.VideoPlayerController
import chaintech.videoplayer.ui.video.rememberVideoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
@Composable
fun ReelsPlayerView(
    modifier: Modifier = Modifier, // Modifier for the composable
    urls: List<String>, // List of video URLs
    playerConfig: PlayerConfig = PlayerConfig(), // Configuration for the player
    controller: VideoPlayerController = rememberVideoPlayer(
        source = mutableStateOf(VideoSource.Url(urls.firstOrNull() ?: "")),
        playerConfig = playerConfig
    ),
    playButtonBuilder: PlayButtonBuilder? = null,
    progressBuilder: ProgressBuilder? = null,
    componentBuilder: ComponentBuilder? = null,
) {
    // Remember the state of the pager
    val pagerState = rememberPagerState(pageCount = {
        urls.size // Set the page count based on the number of URLs
    })
    val isCurrentPause = remember{ mutableStateOf(false) }
    // Animate scrolling to the current page when it changes
    LaunchedEffect(key1 = pagerState) {
        snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { page ->
            controller.changeSource(VideoSource.Url(urls[page]))
            pagerState.animateScrollToPage(page)
        }
    }

    val showControls = remember { controller.showControls } // State for showing/hiding controls
    var isSeekbarSliding = false // Flag for indicating if the seek bar is being slid
    val isFullScreen = remember { controller.isFullScreen }
    val isMute = remember { controller.isMute }
    val selectedSpeed = remember { controller.selectedSpeed } // Selected playback speed


    // Auto-hide controls if enabled
    if (controller.playerConfig.isAutoHideControlEnabled) {
        LaunchedEffect(showControls.value,isCurrentPause.value) {
            if (showControls.value&& isCurrentPause.value.not()) {
                delay(timeMillis = (controller.playerConfig.controlHideIntervalSeconds * 1000).toLong()) // Delay hiding controls
                if (isSeekbarSliding.not()  && isCurrentPause.value.not()) {
                    showControls.value = false // Hide controls if seek bar is not being slid
                }
            }else if(!showControls.value && isCurrentPause.value){
                showControls.value = true
            }
        }
    }

    // Render vertical pager if enabled, otherwise render horizontal pager
    if (controller.playerConfig.reelVerticalScrolling) {
        VerticalPager(
            state = pagerState,
        ) { page ->
            val isPause = remember { mutableStateOf(false) } // State for pausing/resuming video
            LaunchedEffect(page,isPause.value){
                isCurrentPause.value=isPause.value
            }
            // Video player with control
            VideoPlayerWithControl(
                modifier = modifier,
//                url = urls[page], // URL of the video
                controller = controller, // URL of the video
                playerConfig = controller.playerConfig,
                isMute = isMute,
                selectedSpeed = selectedSpeed,
                isPause = if (pagerState.currentPage == page) {
                    isPause
                } else {
                    mutableStateOf(true)
                }, // Pause video when not in focus
                onPauseToggle = { isPause.value = isPause.value.not() }, // Toggle pause/resume
                showControls = showControls, // Show/hide controls
                onShowControlsToggle = {
                    showControls.value = showControls.value.not()
                }, // Toggle show/hide controls
                onChangeSeekbar = { isSeekbarSliding = it }, // Update seek bar sliding state
                isFullScreen = isFullScreen,
                onFullScreenToggle = { isFullScreen.value = isFullScreen.value.not() },
                playButtonBuilder = playButtonBuilder,
                progressBuilder = progressBuilder,
                cover = null,
                onAutomaticallyPause = {
                    isCurrentPause.value = true
                },
                componentBuilder = componentBuilder
            )
        }
    } else {
        HorizontalPager(
            state = pagerState
        ) { page ->
            val isPause = remember { mutableStateOf(false) } // State for pausing/resuming video
            // Video player with control
            VideoPlayerWithControl(
                modifier = modifier,
//                url = urls[page], // URL of the video
                controller = controller, // URL of the video
                isMute = isMute,
                selectedSpeed = selectedSpeed,
                playerConfig = controller.playerConfig,
                isPause = if (pagerState.currentPage == page) {
                    isPause
                } else {
                    mutableStateOf(true)
                }, // Pause video when not in focus
                onPauseToggle = { isPause.value = isPause.value.not() }, // Toggle pause/resume
                showControls = showControls, // Show/hide controls
                onShowControlsToggle = {
                    showControls.value = showControls.value.not()
                }, // Toggle show/hide controls
                onChangeSeekbar = { isSeekbarSliding = it }, // Update seek bar sliding state
                isFullScreen = isFullScreen,
                onFullScreenToggle = { isFullScreen.value = isFullScreen.value.not() },
                playButtonBuilder = playButtonBuilder,
                progressBuilder = progressBuilder,
                cover = null,
                onAutomaticallyPause = {
                    isCurrentPause.value = true
                },
                componentBuilder = componentBuilder
            )
        }
    }
}

