package chaintech.videoplayer.ui.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import chaintech.videoplayer.extension.formatMinSec
import chaintech.videoplayer.model.PlayerConfig
import chaintech.videoplayer.ui.slider.ColorfulSlider
import chaintech.videoplayer.ui.slider.MaterialSliderDefaults
import chaintech.videoplayer.ui.slider.MaterialSliderDefaults.toBrush
import chaintech.videoplayer.ui.video.controls.VideoPlayerController
import chaintech.videoplayer.util.LandscapeOrientation
import com.github.jing332.filepicker.base.FileImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext
import mediaplayer.generated.resources.Res
import mediaplayer.generated.resources.forward_10_24px
import mediaplayer.generated.resources.replay_10_24px
import org.jetbrains.compose.resources.vectorResource


@Composable
fun rememberVideoPlayer(
    source: MutableState<VideoSource>,
    playerConfig: PlayerConfig = PlayerConfig()
): VideoPlayerController {
    val controller = remember(source.value) {
        VideoPlayerController(source.value, playerConfig).apply {
            isPause.value = true
        }
    }
    LaunchedEffect(source.value) {
        controller.changeSource(source.value)
        controller.currentPlayingTime.value = 0L
        controller.duration.value = 0L
    }
    return controller
}


sealed class VideoSource {
    data class Url(val url: String) : VideoSource()
    data class File(val file: FileImpl) : VideoSource()
}


typealias PlayButtonBuilder = @Composable @UiComposable() BoxWithConstraintsScope.(
    controller: VideoPlayerController, showControls: MutableState<Boolean>,
    isPause: MutableState<Boolean>,
    onPauseToggle: () -> Unit,
    onBackwardToggle: () -> Unit,
    onForwardToggle: () -> Unit,
) -> Unit

//typealias ProgressBuilder = @Composable @UiComposable() BoxWithConstraintsScope.(
//    controller: VideoPlayerController,showControls:Boolean,
//    isPause: MutableState<Boolean>,
//    onPauseToggle: () -> Unit,
//    current: Int, total: Int,
//    onChangeSliding: (Boolean) -> Unit,
//    onChangeSliderTime: (Int?) -> Unit,
//    onChangeCurrentTime: (Int) -> Unit,
//) -> Unit

interface ProgressBuilder {
    @Composable
    @UiComposable
    fun build(
        scope: BoxWithConstraintsScope,
        controller: VideoPlayerController, showControls: MutableState<Boolean>,
        isPause: MutableState<Boolean>,
        onPauseToggle: () -> Unit,
        current: Int, total: Int,
        onChangeSliding: (Boolean) -> Unit,
        onChangeSliderTime: (Int?) -> Unit,
        onChangeCurrentTime: (Int) -> Unit
    )
}

typealias ComponentBuilder = @Composable @UiComposable() BoxWithConstraintsScope.(
    controller: VideoPlayerController, showControls: MutableState<Boolean>,
    showSpeedSelection: MutableState<Boolean>,
    onFullScreenToggle: () -> Unit,
    screenLocked: MutableState<Boolean>
) -> Unit


@Composable
fun SimpleVideoPlayerView(
    modifier: Modifier = Modifier,
    controller: VideoPlayerController,
    gestureEnable: Boolean = true,
    hideControls: Boolean = false,
    needSeek: Boolean = false,
    canPlay: Boolean = true,
    cover: String?,
    enableDrag: MutableState<Boolean> = mutableStateOf(true),
    enableAcceleration: Boolean = true,
    onAutomaticallyPause: () -> Unit,
    onPauseToggle: () -> Boolean,
    slideUnAcceptable: () -> Unit = {},
    tooltipCompose: (@Composable RowScope.(showSpeedSelection: MutableState<Boolean>) -> Unit)? = null,
    component: (@Composable BoxWithConstraintsScope.(
        controller: VideoPlayerController, showControls: MutableState<Boolean>,
        showSpeedSelection: MutableState<Boolean>,
        onFullScreenToggle: () -> Unit,
        screenLocked: MutableState<Boolean>
    ) -> Unit)? = null,
    content:@Composable BoxWithConstraintsScope.(VideoPlayerController)->Unit = {},
) {
    var size by remember { mutableStateOf(Size.Unspecified) }
    with(LocalDensity.current) {
        Box(modifier) {
            VideoPlayerView(
                modifier.then(Modifier.onGloballyPositioned {
                    size = it.size.toSize()
                }),
                controller.source,
                controller.playerConfig,
                controller,
                cover = cover,
                needSeek = needSeek,
                onAutomaticallyPause = onAutomaticallyPause,
                gestureEnable = gestureEnable,
                playButtonBuilder = { cons, showControls, isPause, onPauseToggle, onBackward, onForward ->
                    val showing =
                        animateFloatAsState(
                            targetValue = if (showControls.value) 1f else 0f,
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = EaseInOutCubic
                            )
                        ).value
                    if (showing > 0f && hideControls.not()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .requiredSizeIn(maxHeight = maxHeight, maxWidth = maxWidth)
                                .wrapContentSize()
                                .alpha(showing),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedVisibility(visible = canPlay) {
                                Row(
                                    modifier = Modifier
                                        .wrapContentSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    val isCanBackward = enableDrag.value
                                    AnimatedVisibility(
                                        visible = isCanBackward && canPlay,
                                    ) {
                                        var currentRotationAngle by remember { mutableStateOf(0f) } // 当前累积旋转角度
                                        val rotationAngle by animateFloatAsState(
                                            targetValue = currentRotationAngle, // 点击时旋转360度
                                            animationSpec = tween(durationMillis = 500) // 动画时长500ms
                                        )
                                        IconButton(
                                            enabled = isCanBackward && canPlay,
                                            onClick = {
                                                if (isPause.value.not()) {
                                                    currentRotationAngle -= 360f // 每次点击增加360度
                                                    onBackward()
                                                }
                                            },
                                            modifier = Modifier.rotate(rotationAngle)
                                                .size(64.dp)
                                                .padding(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = vectorResource(Res.drawable.replay_10_24px),
                                                contentDescription = "Rewind",
                                                modifier = Modifier.fillMaxSize(0.6f),
                                                tint = if (enableDrag.value) Color.White else Color.Transparent
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(if (size.isUnspecified) 0.dp else size.width.toDp() * 0.1f))
                                    IconButton(
                                        onClick = onPauseToggle,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .padding(8.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = Color.White
                                        ),
                                    ) {
                                        Icon(
                                            imageVector = if (!isPause.value) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = if (!isPause.value) "Pause" else "Play",
                                            modifier = Modifier.fillMaxSize(),
                                            tint = Color.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(if (size.isUnspecified) 0.dp else size.width.toDp() * 0.1f))

                                    val isCanForward = enableDrag.value



                                    AnimatedVisibility(
                                        visible = isCanForward && canPlay,
                                        modifier = Modifier
                                    ) {

                                        var currentRotationAngle by remember { mutableStateOf(0f) } // 当前累积旋转角度

                                        val rotationAngle by animateFloatAsState(
                                            targetValue = currentRotationAngle, // 点击时旋转360度
                                            animationSpec = tween(durationMillis = 500) // 动画时长500ms
                                        )
                                        IconButton(
                                            enabled = isCanForward && canPlay,
                                            onClick = {
                                                if (isPause.value.not()) {
                                                    currentRotationAngle += 360f // 每次点击增加360度
                                                    onForward()
                                                }
                                            },
                                            modifier = Modifier.rotate(rotationAngle) // 设置旋转角度
                                                .size(64.dp)
                                                .padding(8.dp)
                                        ) {
                                            Icon(
                                                modifier = Modifier.fillMaxSize(0.6f),
                                                imageVector = vectorResource(Res.drawable.forward_10_24px),
                                                contentDescription = "Forward",
                                                tint = if (isCanForward) Color.White else Color.Transparent
                                            )
                                        }
                                    }

                                }
                            }
                        }
                    }
                },
                progressBuilder = object : ProgressBuilder {
                    @Composable
                    override fun build(
                        scope: BoxWithConstraintsScope,
                        controller: VideoPlayerController,
                        showControls: MutableState<Boolean>,
                        isPause: MutableState<Boolean>,
                        onPauseToggle: () -> Unit,
                        current: Int,
                        total: Int,
                        onChangeSliding: (Boolean) -> Unit,
                        onChangeSliderTime: (Int?) -> Unit,
                        onChangeCurrentTime: (Int) -> Unit
                    ) {

                        scope.apply {
                            val showing =
                                animateFloatAsState(
                                    targetValue = if (showControls.value) 1f else 0f,
                                    animationSpec = tween(
                                        durationMillis = 600,
                                        easing = EaseInOutCubic
                                    )
                                ).value
                            if (showing > 0f && hideControls.not()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .requiredSizeIn(
                                            maxHeight = maxHeight,
                                            maxWidth = maxWidth
                                        )
                                        .animateContentSize()
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .offset(0.dp, 40.dp * (1f - showing))
                                        .alpha(showing),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f))
                                            .padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(
                                            enabled = canPlay,
                                            onClick = onPauseToggle,
                                            modifier = Modifier
                                                .size(25.dp),
                                        ) {
                                            Icon(
                                                imageVector = if (!isPause.value) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                contentDescription = if (!isPause.value) "Pause" else "Play",
                                                tint = Color.White
                                            )
                                        }

                                        Text(
                                            current.coerceAtLeast(0).formatMinSec(),
                                            modifier = Modifier.padding(horizontal = 5.dp),
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )

                                        var slideProgress = current

                                        val tapEnabled = mutableStateOf(
                                            total > 0 && (enableDrag.value && canPlay) &&
                                                    (controller.seekPos.value).toInt() < total
                                        )

                                        LaunchedEffect(Unit) {
                                            snapshotFlow { slideProgress }
                                                .distinctUntilChanged()
                                                .filter { total > 0 }
                                                .collect {
                                                    tapEnabled.value =
                                                        total > 0 && (enableDrag.value && canPlay) &&
                                                                (controller.seekPos.value).toInt() < total
                                                }
                                        }
                                        ColorfulSlider(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(15.dp),
                                            value = current.toFloat(), // Current value of the slider
                                            enabled = true, // Enable the slider
                                            onValueChange = { second, offset ->
                                                if (enableDrag.value && canPlay) {
                                                    slideProgress = second.toInt()
                                                    onChangeSliding(true) // Indicate sliding state
                                                    onChangeSliderTime(null) // Reset slider time change callback
                                                    onChangeCurrentTime(second.toInt()) // Update current time
                                                } else {
                                                    slideProgress = second.toInt()
                                                    onChangeSliding(true) // Indicate sliding state
                                                    onChangeSliderTime(null) // Reset slider time change callback
                                                    onChangeCurrentTime(second.toInt()) // Update current time
                                                }
                                            },
                                            tapEnabled = tapEnabled,//是否可以点击
                                            valueRange = 0f..total.toFloat(), // Range of the slider
                                            onValueChangeFinished = {
                                                val maxDragValue = controller.seekPos.value.toInt()
                                                var shouldReset = false
                                                if ((enableDrag.value && canPlay).not()) {
                                                    if (slideProgress > maxDragValue) {
                                                        slideProgress = maxDragValue
                                                        shouldReset = true
                                                        onChangeSliderTime(slideProgress) // Update slider time change callback
                                                        slideUnAcceptable()
                                                    }
                                                }
                                                onChangeSliding(false) // Indicate sliding state finished
                                                if (shouldReset.not()) {
                                                    onChangeSliderTime(slideProgress) // Update slider time change callback
                                                    onChangeCurrentTime(slideProgress)
                                                }
                                            },
                                            colors = MaterialSliderDefaults.defaultColors(
                                                disabledThumbColor = controller.playerConfig.seekBarThumbColor.toBrush(),
                                                disabledActiveTrackColor = controller.playerConfig.seekBarActiveTrackColor.toBrush(),
                                                disabledInactiveTrackColor = controller.playerConfig.seekBarInactiveTrackColor.toBrush(),
                                                thumbColor = controller.playerConfig.seekBarThumbColor.toBrush(),
                                                inactiveTrackColor = controller.playerConfig.seekBarInactiveTrackColor.toBrush(),
                                                activeTrackColor = controller.playerConfig.seekBarActiveTrackColor.toBrush()
                                            )
                                        )

                                        Text(
                                            (total - current).coerceAtLeast(0)
                                                .formatMinSec(),
                                            modifier = Modifier.padding(start = 5.dp),
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                componentBuilder = { cont, showControls, showSpeedSelection, fullToggle, screenLock ->
                    if (canPlay) {
                        val showing =
                            animateFloatAsState(
                                targetValue = if (showControls.value) 1f else 0f,
                                animationSpec = tween(
                                    durationMillis = 600,
                                    easing = EaseInOutCubic
                                )
                            ).value
                        if (showing > 0f && hideControls.not()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .requiredSizeIn(maxHeight = maxHeight, maxWidth = maxWidth)
                                    .wrapContentSize()
                                    .alpha(showing),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .padding(top = 10.dp, end = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(
                                        10.dp,
                                        Alignment.CenterHorizontally
                                    )
                                ) {
                                    var expanded by remember { mutableStateOf(false) }
                                    LaunchedEffect(showControls.value) {
                                        if (!showControls.value) {
                                            withContext(Dispatchers.IO) {
                                                delay(500)
                                                if (!showControls.value) {
                                                    expanded = false
                                                }
                                            }
                                        }
                                    }
                                    AnimatedVisibility(
                                        visible = cont.isFullScreen.value && (expanded || screenLock.value),
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        IconButton(
                                            onClick = { screenLock.value = !screenLock.value },
                                            modifier = Modifier.size(30.dp),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = Color.White
                                            )
                                        ) {
                                            Icon(
                                                imageVector = if (screenLock.value) Icons.Default.LockClock else Icons.Default.LockOpen,
                                                contentDescription = "Lock",
                                                tint = Color.Black
                                            )
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = !cont.isScreenLocked.value,
                                        modifier = Modifier.height(30.dp)
                                            .wrapContentWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxHeight()
                                                .wrapContentWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(
                                                10.dp,
                                                Alignment.CenterHorizontally
                                            )
                                        ) {
                                            if (expanded) {
                                                if (tooltipCompose != null) {
                                                    tooltipCompose(this, showSpeedSelection)
                                                }
                                                AnimatedVisibility(
                                                    visible = enableAcceleration,
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    IconButton(
                                                        enabled = enableAcceleration,
                                                        onClick = {
                                                            showSpeedSelection.value =
                                                                showSpeedSelection.value.not()
                                                        },
                                                        modifier = Modifier.size(30.dp),
                                                        colors = IconButtonDefaults.iconButtonColors(
                                                            containerColor = Color.White
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Speed,
                                                            contentDescription = "Speed",
                                                            tint = Color.Black
                                                        )
                                                    }
                                                }

                                                IconButton(
                                                    onClick = {
                                                        cont.isMute.value =
                                                            cont.isMute.value.not()
                                                    },
                                                    modifier = Modifier.size(30.dp),
                                                    colors = IconButtonDefaults.iconButtonColors(
                                                        containerColor = Color.White
                                                    )
                                                ) {
                                                    Icon(
                                                        imageVector = if (cont.isMute.value) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                                        contentDescription = "Mute",
                                                        tint = Color.Black
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    "展开",
                                                    modifier = Modifier
                                                        .background(
                                                            Color.White,
                                                            RoundedCornerShape(5.dp)
                                                        )
                                                        .padding(5.dp)
                                                        .wrapContentSize()
                                                        .clickable {
                                                            expanded = true
                                                        }, fontSize = 14.sp, color = Color.Black
                                                )
                                            }

                                            IconButton(
                                                onClick = { fullToggle() },
                                                modifier = Modifier.size(30.dp),
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    containerColor = Color.White
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = if (cont.isFullScreen.value) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                                    contentDescription = "Fullscreen",
                                                    tint = Color.Black
                                                )
                                            }


                                        }
                                    }

                                }
                            }
                        }
                        if (component != null) {
                            component(
                                this,
                                controller,
                                showControls,
                                showSpeedSelection,
                                fullToggle,
                                screenLock
                            )
                        }
                    }
                },
                onPauseToggle = onPauseToggle,content = content
            )


        }

        DisposableEffect(Unit) {
            onDispose {
                controller.isPause.value = true
            }
        }
    }
}


@Composable
fun VideoPlayerView(
    modifier: Modifier = Modifier, // Modifier for the composable
    source: MutableState<VideoSource> = mutableStateOf(VideoSource.Url("")), // URL of the video
    playerConfig: PlayerConfig = PlayerConfig(), // Configuration for the player
    controller: VideoPlayerController = rememberVideoPlayer(source, playerConfig = playerConfig),
    gestureEnable: Boolean = true,
    needSeek: Boolean = false,
    cover: String?,
    onAutomaticallyPause: () -> Unit,
    isLooping: Boolean = gestureEnable.not(),
    playButtonBuilder: PlayButtonBuilder? = null,
    progressBuilder: ProgressBuilder? = null,
    componentBuilder: ComponentBuilder? = null,
    onPauseToggle: () -> Boolean,
    content:@Composable BoxWithConstraintsScope.(VideoPlayerController)->Unit = {},
) {
    val isPause = remember { controller.isPause } // State for pausing/resuming video
    val showControls = remember { controller.showControls } // State for showing/hiding controls
    val isSeekbarSliding =
        remember { mutableStateOf(false) } // Flag for indicating if the seek bar is being slid
    val isFullScreen = remember { controller.isFullScreen }
    val isMute = remember { controller.isMute }
    val selectedSpeed = remember { controller.selectedSpeed } // Selected playback speed


    // Auto-hide controls if enabled
    if (controller.playerConfig.isAutoHideControlEnabled) {
        LaunchedEffect(showControls.value, isPause.value, isSeekbarSliding.value) {
            if (showControls.value && isPause.value.not()) {
                delay(timeMillis = (controller.playerConfig.controlHideIntervalSeconds * 1000).toLong()) // Delay hiding controls
                if (isSeekbarSliding.value.not() && isPause.value.not()) {
                    showControls.value = false // Hide controls if seek bar is not being slid
                }
            } else if (!showControls.value && isPause.value) {
                showControls.value = true
            }
        }
    }

    //  旋转屏幕
    LandscapeOrientation(isFullScreen) {
        // Video player with control
        VideoPlayerWithControl(
            modifier = if (isFullScreen.value) {
                Modifier.fillMaxSize()
            } else {
                modifier
            },
            controller = controller, // URL of the video
            needSeek = needSeek,
            isMute = isMute,
            isLooping = isLooping,
            cover = cover,
            onAutomaticallyPause = onAutomaticallyPause,
            gestureEnable = gestureEnable,
            selectedSpeed = selectedSpeed,
            playerConfig = controller.playerConfig, // Player configuration
            isPause = isPause, // Flag indicating if the video is paused
            onPauseToggle = {
                if (onPauseToggle()) {
                    isPause.value = isPause.value.not()
                }
            }, // Toggle pause/resume
            showControls = showControls, // Flag indicating if controls should be shown
            onShowControlsToggle = {
                showControls.value = showControls.value.not()
            }, // Toggle show/hide controls
            onChangeSeekbar = { isSeekbarSliding.value = it }, // Update seek bar sliding state
            isFullScreen = isFullScreen,
            onFullScreenToggle = { isFullScreen.value = isFullScreen.value.not() },
            playButtonBuilder = playButtonBuilder,
            progressBuilder = progressBuilder,
            componentBuilder = componentBuilder,
            content = content
        )
    }
}








