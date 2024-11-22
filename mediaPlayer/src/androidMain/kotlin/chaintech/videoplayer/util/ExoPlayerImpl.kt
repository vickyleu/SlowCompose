package chaintech.videoplayer.util

import android.util.Log
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlaybackException
import chaintech.videoplayer.model.PlayerSpeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CMPPlayer2(
    modifier: Modifier,
    url: String,
    isPause: State<Boolean>,
    isMute: State<Boolean>,
    totalTime: (Int) -> Unit,
    currentTime: (Int) -> Unit,
    isSliding: State<Boolean>,
    sliderTime: MutableState<Int?>,
    speed: MutableState<PlayerSpeed>
) {
    val context = LocalContext.current
    val exoPlayer = rememberExoPlayerWithLifecycle(url, context, isPause)
    val playerView = rememberPlayerView(exoPlayer, context)
    LaunchedEffect(exoPlayer) {
        withContext(Dispatchers.IO) {
            while (isActive) {
                withContext(Dispatchers.Main) {
                    currentTime(exoPlayer.currentPosition.toInt().coerceAtLeast(0))
                }
                delay(1000L)
            }
        }
    }

    val scope = rememberCoroutineScope()
    LaunchedEffect(playerView) {
        playerView.keepScreenOn = true
    }
    Box(modifier = modifier) {
        AndroidView(
            factory = {
                FrameLayout(it).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.Black.toArgb())
                    viewTreeObserver.addOnPreDrawListener(object :
                        ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            viewTreeObserver.removeOnPreDrawListener(this)
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    withContext(Dispatchers.Main) {
                                        exoPlayer.prepare()
                                    }
                                    delay(600)
                                    withContext(Dispatchers.Main) {
                                        if (playerView.parent == null) {
                                            addView(
                                                playerView,
                                                FrameLayout.LayoutParams(
                                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                                    FrameLayout.LayoutParams.MATCH_PARENT
                                                )
                                            )
                                            if (playerView.player == null) {
                                                playerView.player = exoPlayer
                                                exoPlayer.volume = if (isMute.value) 0f else 1f
                                                sliderTime.value?.let { exoPlayer.seekTo(it.toLong()) }
                                                exoPlayer.setPlaybackSpeed(
                                                    when (speed.value) {
                                                        PlayerSpeed.X0_5 -> 0.5f
                                                        PlayerSpeed.X1 -> 1.0f
                                                        PlayerSpeed.X1_5 -> 1.5f
                                                        PlayerSpeed.X2 -> 2.0f
                                                    }
                                                )

                                            }
                                            val play = !isPause.value
                                            exoPlayer.playWhenReady = play
                                        }
                                    }
                                }
                            }
                            return true
                        }
                    })
                }
            },
            modifier = Modifier
                .fillMaxSize(),
            update = { parent ->

                /*else{
                    val play = !isPause.value
                    exoPlayer.setPlaybackSpeed(
                        when (speed) {
                            PlayerSpeed.X0_5 -> 0.5f
                            PlayerSpeed.X1 -> 1.0f
                            PlayerSpeed.X1_5 -> 1.5f
                            PlayerSpeed.X2 -> 2.0f
                        }
                    )
                    exoPlayer.playWhenReady = play
                }*/
            }
        )

        DisposableEffect(Unit) {
            val job = scope.launch {
                snapshotFlow { isPause.value }
                    .drop(1)
                    .map { it.not() }
                    .distinctUntilChanged()
                    .filter { exoPlayer.playWhenReady != it }
                    .collect {
                        exoPlayer.playWhenReady = it
                    }
            }
            onDispose {
                job.cancel()
            }
        }

        DisposableEffect(Unit) {
            val listener = object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    if (!isSliding.value) {
                        totalTime(player.duration.coerceAtLeast(0).toInt())
                        currentTime(player.currentPosition.coerceAtLeast(0).toInt())
                    }
                }

                private val TAG = "CMPPlayer"

                @OptIn(UnstableApi::class)
                override fun onPlayerError(error: PlaybackException) {
                    if (error is ExoPlaybackException) {
                        when (error.type) {
                            ExoPlaybackException.TYPE_SOURCE -> {
                                Log.e(
                                    TAG,
                                    "TYPE_SOURCE: " + error.sourceException.message
                                )
                            }

                            ExoPlaybackException.TYPE_RENDERER -> {
                                Log.e(
                                    TAG,
                                    "TYPE_RENDERER: " + error.rendererException.message
                                )
                            }

                            ExoPlaybackException.TYPE_UNEXPECTED -> {
                                Log.e(
                                    TAG,
                                    "TYPE_UNEXPECTED: " + error.unexpectedException.message
                                )
                            }

                            ExoPlaybackException.TYPE_REMOTE -> {
                                Log.e(
                                    TAG,
                                    "TYPE_REMOTE: " + error.message
                                )
                            }
                        }
                    } else {
                        Log.e(TAG, "TYPE_SOURCE: " + (error.cause?.message ?: error.errorCodeName))
                    }
                }
            }
            exoPlayer.addListener(listener)
            onDispose {
                exoPlayer.removeListener(listener)
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { sliderTime.value }
            .filterNotNull()
            .distinctUntilChanged()
            .collect {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        exoPlayer.seekTo(it.toLong())
                    }
                }
            }
    }
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            playerView.player = null
        }
    }
}