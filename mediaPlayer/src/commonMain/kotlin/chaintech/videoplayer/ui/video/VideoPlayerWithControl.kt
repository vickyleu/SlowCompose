package chaintech.videoplayer.ui.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.extension.formattedInterval
import chaintech.videoplayer.model.PlayerConfig
import chaintech.videoplayer.model.PlayerSpeed
import chaintech.videoplayer.model.gradientBGColors
import chaintech.videoplayer.ui.component.SpeedSelectionView
import chaintech.videoplayer.ui.video.controls.BottomControlView
import chaintech.videoplayer.ui.video.controls.CenterControlView
import chaintech.videoplayer.ui.video.controls.LockScreenView
import chaintech.videoplayer.ui.video.controls.TopControlView
import chaintech.videoplayer.ui.video.controls.VideoPlayerController
import chaintech.videoplayer.util.CMPPlayerCompose
import chaintech.videoplayer.util.getCurrentFirstFrame
import chaintech.videoplayer.util.release
import chaintech.videoplayer.util.seekTo
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VideoPlayerWithControl(
    modifier: Modifier,
    gestureEnable: Boolean = true,
    needSeek: Boolean = false,
    isLooping: Boolean = false, // Flag indicating if the video should loop
    controller: VideoPlayerController, // URL of the video
    isMute: MutableState<Boolean>, // URL of the video
    selectedSpeed: MutableState<PlayerSpeed>, // URL of the video
    playerConfig: PlayerConfig, // Configuration for the player
    isPause: MutableState<Boolean>, // Flag indicating if the video is paused
    onPauseToggle: () -> Unit, // Callback for toggling pause/resume
    showControls: MutableState<Boolean>, // Flag indicating if controls should be shown
    cover: String?,
    onShowControlsToggle: () -> Unit, // Callback for toggling show/hide controls
    onChangeSeekbar: (Boolean) -> Unit, // Callback for seek bar sliding
    isFullScreen: MutableState<Boolean>,
    onAutomaticallyPause: () -> Unit,
    onFullScreenToggle: () -> Unit,
    playButtonBuilder: PlayButtonBuilder? = null,
    progressBuilder: ProgressBuilder? = null,
    componentBuilder: ComponentBuilder? = null,
    content:@Composable BoxWithConstraintsScope.(VideoPlayerController)->Unit = {},
) {
    val controllerDelegate = remember { controller }
    val totalTime = remember { mutableStateOf(0) } // Total duration of the video
    val currentTime = remember { mutableStateOf(0) } // Current playback time
    val isSliding =
        remember { mutableStateOf(false) } // Flag indicating if the seek bar is being slid
    val sliderTime = remember { mutableStateOf<Int?>(null) } // Time indicated by the seek bar
    val showSpeedSelection = remember { mutableStateOf(false) } // Selected playback speed
    val isScreenLocked = remember { controllerDelegate.isScreenLocked }

    // Container for the video player and control components
    BoxWithConstraints(
        modifier = modifier
            .let {
                if (gestureEnable) {
                    it.pointerInput(Unit) {
                        detectTapGestures { _ ->
                            if (isPause.value.not() || !showControls.value) {
                                onShowControlsToggle() // Toggle show/hide controls on tap
                            }
                            showSpeedSelection.value = false
                        }
                    }
                } else it
            }
    ) {

        LaunchedEffect(Unit){
            snapshotFlow { totalTime.value }
                .distinctUntilChanged()
                .filter { it > 0 }
                .collect{
                    controllerDelegate.duration.value = it.toLong()
                }
        }
        LaunchedEffect(Unit){
            snapshotFlow { currentTime.value }
                .distinctUntilChanged()
                .filter { it > 0 }
                .collect{
                    controllerDelegate.currentPlayingTime.value = it.toLong()
                }
        }
        val imageBitmap =
            remember { mutableStateOf<ImageBitmap?>(null) }

        LaunchedEffect(controllerDelegate.source.value){
            currentTime.value = 0
            totalTime.value = 0
            imageBitmap.value = null
//            sliderTime.value = null
        }



        val player = CMPPlayerCompose(
            modifier = modifier.background(Color.Black),
            source = controllerDelegate.source,
            forceUpdate = controllerDelegate.forceUpdate,
            isPause = isPause,
            isMute = isMute,
            isLooping = isLooping,
            totalTime = totalTime, // Update total time of the video
            seekPos= if(needSeek)controllerDelegate.seekPos else mutableStateOf(0f),
            currentTime = {
                if (isSliding.value.not()) {
                    println("播放结束了吗:$it")
                    currentTime.value = it // Update current playback time
                    sliderTime.value = null // Reset slider time if not sliding
                }
            },
            onAutomaticallyPause = onAutomaticallyPause,
            isSliding = isSliding, // Pass seek bar sliding state
            sliderTime = sliderTime, // Pass seek bar slider time
            speed = selectedSpeed, // Pass selected playback speed
            fullscreen = isFullScreen
        )

        content.invoke(this,controllerDelegate)

        DisposableEffect(Unit) {
            onDispose {
                player.release()
            }
        }

        BoxWithConstraints(
            modifier = Modifier.matchParentSize()
        ) {
            if (!cover.isNullOrBlank()) {
                if (isPause.value && currentTime.value == 0) {
                    Coil(
                        data = cover,
                        contentDescription = "cover",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(maxWidth, maxHeight).background(Color.Black)
                    )
                }
            } else if (imageBitmap.value != null) {
                if (isPause.value && currentTime.value == 0) {
                    androidx.compose.foundation.Image(
                        bitmap = imageBitmap.value!!,
                        contentDescription = "cover",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(maxWidth, maxHeight).background(Color.Black)
                    )
                }
            }
        }


        LaunchedEffect(controllerDelegate.source.value){
            snapshotFlow { controllerDelegate.currentPlayingTime.value }
                .distinctUntilChanged()
                .filter { needSeek && isSliding.value.not() }
                .map { it.toFloat()/1000f }
                .filter {
                    it>controllerDelegate.seekPos.value
                }
                .collect{
                    controllerDelegate.seekPos.value = it
                }
        }

        LaunchedEffect(controllerDelegate.source.value,controllerDelegate.duration.value){
            if(needSeek && controllerDelegate.duration.value>0){
                if(((controllerDelegate.seekPos.value*1000L)+1000L)< controllerDelegate.duration.value){
                    println("seekTo: ${(controllerDelegate.seekPos.value*1000L).toInt()}")
                    player.seekTo((controllerDelegate.seekPos.value*1000L).toLong())
                }
            }
        }

        val scope = rememberCoroutineScope()
        var job by remember { mutableStateOf<Job?>(null) }
        DisposableEffect(Unit) {
            if (cover == null) {
                job = scope.launch {
                    snapshotFlow { totalTime.value }
                        .filter { it > 0 }
                        .distinctUntilChanged()
                        .filter { imageBitmap.value == null }
                        .collect {
                            scope.launch {
                                withContext(Dispatchers.IO){
                                    imageBitmap.value = player.getCurrentFirstFrame(this@BoxWithConstraints.constraints.let{
                                        Size(it.maxWidth.toFloat(),it.maxHeight.toFloat())
                                    })
                                }
                            }
                        }
                }
            }
            onDispose {
                job?.cancel()
            }
        }

        if(gestureEnable){
            LaunchedEffect(controllerDelegate.source.value){
                if(showControls.value.not()){
                    onShowControlsToggle()
                }
            }
        }

        if (isScreenLocked.value.not()) {
            // Top control view for playback speed and mute/unMute
            if (componentBuilder == null) {
                TopControlView(
                    playerConfig = playerConfig,
                    isMute = isMute,
                    onMuteToggle = { isMute.value = isMute.value.not() }, // Toggle mute/unMute
                    showControls = showControls, // Pass show/hide controls state
                    onTapSpeed = { showSpeedSelection.value = showSpeedSelection.value.not() },
                    isFullScreen = isFullScreen,
                    onFullScreenToggle = { onFullScreenToggle() },
                    onLockScreenToggle = { isScreenLocked.value = isScreenLocked.value.not() }
                )
            }

            if (playButtonBuilder != null) {
                playButtonBuilder(
                    this,
                    controllerDelegate,
                    showControls,
                    isPause,
                    onPauseToggle,
                    {
                        if(isPause.value)return@playButtonBuilder
                        isSliding.value = true
                        val newTime =
                            currentTime.value - playerConfig.fastForwardBackwardIntervalSeconds.formattedInterval()
                        sliderTime.value = if (newTime < 0) {
                            0
                        } else {
                            newTime
                        }
                        player.seekTo(sliderTime.value!!.toLong())
                        isSliding.value = false
                    },
                    {
                        if(isPause.value)return@playButtonBuilder
                        isSliding.value = true
                        val newTime =
                            currentTime.value + playerConfig.fastForwardBackwardIntervalSeconds.formattedInterval()
                        sliderTime.value = if (newTime > totalTime.value) {
                            totalTime.value
                        } else {
                            newTime
                        }
                        player.seekTo(sliderTime.value!!.toLong())
                        isSliding.value = false
                    }
                )
            } else {
                // Center control view for pause/resume and fast forward/backward actions
                CenterControlView(
                    playerConfig = playerConfig,
                    isPause = isPause,
                    onPauseToggle = onPauseToggle,
                    onBackwardToggle = { // Seek backward
                        isSliding.value = true
                        val newTime =
                            currentTime.value - playerConfig.fastForwardBackwardIntervalSeconds.formattedInterval()
                        sliderTime.value = if (newTime < 0) {
                            0
                        } else {
                            newTime
                        }
                        isSliding.value = false
                    },
                    onForwardToggle = { // Seek forward
                        isSliding.value = true
                        val newTime =
                            currentTime.value + playerConfig.fastForwardBackwardIntervalSeconds.formattedInterval()
                        sliderTime.value = if (newTime > totalTime.value) {
                            totalTime.value
                        } else {
                            newTime
                        }
                        isSliding.value = false
                    },
                    showControls = showControls
                )

            }

            if (progressBuilder != null) {
                progressBuilder.build(
                    scope = this, controller = controllerDelegate, showControls = showControls,
                    isPause = isPause, onPauseToggle = onPauseToggle,
                    current = currentTime.value, total = totalTime.value,
                    onChangeSliding = { it: Boolean ->
                        isSliding.value = it
                        onChangeSeekbar(isSliding.value)
                    }, onChangeSliderTime = { it: Int? ->
                        sliderTime.value = it
                    }, onChangeCurrentTime = { it: Int ->
                        currentTime.value = it
                        if(isSliding.value.not()){
                            player.seekTo(it.toLong())
                        }
                    }
                )
            } else {
                // Bottom control view for seek bar and time duration display
                BottomControlView(
                    playerConfig = playerConfig,
                    currentTime = currentTime.value, // Pass current playback time
                    totalTime = totalTime.value, // Pass total duration of the video
                    showControls = showControls, // Pass show/hide controls state
                    onChangeSliderTime = { sliderTime.value = it }, // Update seek bar slider time
                    onChangeCurrentTime = {
                        currentTime.value = it
                    }, // Update current playback time
                    onChangeSliding = { // Update seek bar sliding state
                        isSliding.value = it
                        onChangeSeekbar(isSliding.value)
                    }
                )
            }

        } else {
            if (playerConfig.isScreenLockEnabled) {
                LockScreenView(
                    playerConfig = playerConfig,
                    showControls = showControls,
                    onLockScreenToggle = { isScreenLocked.value = isScreenLocked.value.not() }
                )
            }
        }
        if (componentBuilder != null) {
            if (isScreenLocked.value.not()) {
                componentBuilder(
                    this,
                    controllerDelegate, showControls,
                    showSpeedSelection,
                    onFullScreenToggle,
                    isScreenLocked
                )
            }
        }
        Box {
            //Playback speed options popup
            AnimatedVisibility(
                modifier = Modifier,
                visible = showSpeedSelection.value,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(durationMillis = 700))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = Brush.horizontalGradient(gradientBGColors))
                ) {

                }
            }

            //Playback speed options popup
            AnimatedVisibility(
                modifier = Modifier,
                visible = showSpeedSelection.value,
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth }, // Start from the right edge
                    animationSpec = tween(durationMillis = 500) // Animation duration
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth }, // Slide out to the right edge
                    animationSpec = tween(durationMillis = 500) // Animation duration
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    SpeedSelectionView(
                        buttonSize = (playerConfig.topControlSize * 1.25f),
                        selectedSpeed = selectedSpeed.value,
                        onSelectSpeed = {
                            it?.let {
                                selectedSpeed.value = it
                            }
                            showSpeedSelection.value = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun Coil(
    modifier: Modifier, data: Any,
    contentScale: ContentScale = ContentScale.Crop,
    placeHolder: @Composable (BoxScope.(Float) -> Unit)? = null,
    error: @Composable (BoxScope.(Throwable) -> Unit)? = null,
    colorFilter: ColorFilter? = null,
    contentDescription: String = "${data.hashCode()}"
) {
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier) {
        AsyncImage(
            model = data,
            contentDescription = contentDescription,
            onLoading = {
                isLoading = true
                isError = false
            },
            onSuccess = {
                isLoading = false
                isError = false
            },
            onError = {
                isLoading = false
                isError = true
            },
            contentScale = contentScale,
            colorFilter = colorFilter,
            modifier = Modifier.matchParentSize(),
        )
        if (isLoading) {
            Box(
                modifier.then(
                    Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.6f))
                ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        if (isError) {
            Box(
                Modifier.matchParentSize().background(Color.White)
                    .border(
                        width = 0.5.dp,
                        color = Color.Black.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(0.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
//                Image(
//                    painter = rememberLottiePainter(
//                        composition = composition,
//                        progress = { progress },
//                    ),
//                    modifier = Modifier.matchParentSize(),
//                    contentDescription = "Lottie animation"
//                )
            }
        }
    }

}