package chaintech.videoplayer.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.Window
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import chaintech.videoplayer.model.PlayerSpeed
import chaintech.videoplayer.ui.video.VideoSource
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_AUTO_COMPLETE
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_ERROR
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_NORMAL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.delayFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tv.danmaku.ijk.media.player.IMediaPlayer
import wseemann.media.FFmpegMediaMetadataRetriever
import java.util.Timer
import java.util.TimerTask


private tailrec fun Context.getActivityWindow(): Window? =
    when (this) {
        is Activity -> window
        is ContextWrapper -> baseContext.getActivityWindow()
        else -> null
    }


@SuppressLint("ComposableNaming")
@Composable
actual fun CMPPlayerCompose(
    modifier: Modifier,
    source: State<VideoSource>,
    forceUpdate: State<Boolean>,
    isPause: MutableState<Boolean>,
    isMute: State<Boolean>,
    isLooping: Boolean,
    totalTime: MutableState<Int>,
    seekPos: MutableState<Float>,
    currentTime: (Int) -> Unit,
    onAutomaticallyPause: () -> Unit,
    isSliding: MutableState<Boolean>,
    sliderTime: MutableState<Int?>,
    speed: MutableState<PlayerSpeed>,
    fullscreen: MutableState<Boolean>
): CMPPlayer {
    val context = LocalContext.current


    val scope = rememberCoroutineScope()


    val isStarted = remember(source) { mutableStateOf(false) }


    var gsyFuckingInitialized by remember { mutableStateOf<IMediaPlayer?>(null) }

    val gsyVideoPlayer =
        rememberGsyPlayerWithLifecycle(
            source,
            forceUpdate,
            context,
            isPause,
            onAutomaticallyPause,
            seekPos,totalTime
        )
    DisposableEffect(source.value){
        isStarted.value = false
        val old = totalTime.value
        if (old>0) {
            gsyVideoPlayer.seekOnStart= 0
            currentTime(0)
            totalTime.value = 0
            println("seekTo:seekOnStart=wtf=>> ${seekPos.value}")
        }

        onDispose {
//            onAutomaticallyPause.invoke()
        }
    }

    LaunchedEffect(isStarted.value,totalTime.value,isPause.value) {
        if(isStarted.value && totalTime.value>0 && isPause.value.not()){
            if(((seekPos.value*1000L).toLong()+1000L) <totalTime.value){
                println("seekTo:seekOnStart=wait started=>> ${seekPos.value}  ${totalTime.value}")
//                gsyVideoPlayer.seekOnStart= (seekPos.value*1000).toLong()
                gsyVideoPlayer.seekOnStart= (seekPos.value*1000L).toLong()
            }
        }
    }




    Box(modifier = modifier) {
        AndroidView(
            factory = {
                NoTouchFrameLayout(it).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.Black.toArgb())
                    gsyVideoPlayer.setBackgroundColor(Color.Black.toArgb())
                    addView(gsyVideoPlayer, layoutParams)
                    gsyVideoPlayer.setSpeedPlaying(when (speed.value) {
                        PlayerSpeed.X0_5 -> 0.5f
                        PlayerSpeed.X1 -> 1.0f
                        PlayerSpeed.X1_5 -> 1.5f
                        PlayerSpeed.X2 -> 2.0f
                    },true)
                }
            },
            modifier = Modifier
                .fillMaxSize(),
            update = { parent ->
            }
        )

        LaunchedEffect(gsyFuckingInitialized) {
            snapshotFlow { isMute.value }
                .distinctUntilChanged()
                .collect {
                    gsyFuckingInitialized?.setVolume(0f, if (it) 0f else 1f)// 静音
                }
        }


        LaunchedEffect(isPause.value) {
            snapshotFlow { gsyFuckingInitialized }
                .filterNotNull()
                .distinctUntilChanged()
                .collect {
                    try {
                        if (isPause.value.not()) {
                            it.isLooping = isLooping
                        }
                    } catch (e: Exception) {
                    }
                }
        }

        var playingTimer by remember { mutableStateOf<Timer?>(null) }
        var playingTask by remember { mutableStateOf<TimerTask?>(null) }
        val ORIGIN_INTERVAL_TIME = (30 * 1000L)
        var INTERVAL_TIME by remember { mutableLongStateOf(ORIGIN_INTERVAL_TIME) }

        fun playingTimerClear(){
            playingTimer?.cancel()
            playingTimer = null
            playingTask?.cancel()
            playingTask = null
        }
        fun playSchedule(){
            if (playingTimer != null || playingTask != null) {
                playingTimerClear()
            }
            playingTimer = Timer()
            playingTask = object : TimerTask() {
                override fun run() {
//                   println("${gsyFuckingInitialized?.currentPosition}")
                }
            }
            playingTimer!!.schedule(playingTask, INTERVAL_TIME, INTERVAL_TIME)
        }

        LaunchedEffect(Unit) {
            snapshotFlow { isPause.value }
                .distinctUntilChanged()
                .map { it.not() }
                .collect{
                    if(it){
                        playSchedule()
                    }else{
                        playingTimerClear()
                    }
                }
        }


        LaunchedEffect(Unit) {
            snapshotFlow { speed.value }
                .distinctUntilChanged()
                .map { when (it) {
                    PlayerSpeed.X0_5 -> 0.5f
                    PlayerSpeed.X1 -> 1.0f
                    PlayerSpeed.X1_5 -> 1.5f
                    PlayerSpeed.X2 -> 2.0f
                } }
                .distinctUntilChanged()
                .collect {
                    println("speed.value:${it}")
                    gsyVideoPlayer.setSpeedPlaying(it,true)
                    INTERVAL_TIME =  (ORIGIN_INTERVAL_TIME.toFloat() / it).toLong()
                }
        }
        var job0 = remember { mutableStateOf<Job?>(null) }

        DisposableEffect(source.value) {
            job0.value =  scope.launch {
                withContext(Dispatchers.IO){
                    snapshotFlow { isPause.value }
                        .map { it.not() }
                        .distinctUntilChanged()
                        .onStart { delay(100) }
                        .collect {
                            scope.launch {
                                withContext(Dispatchers.Main){
                                    println("播放isPlaying gsyVideoPlayer ==>> $it  isStarted.value:${isStarted.value}")
                                    if (it) {
                                        if (isStarted.value) {
                                            if (gsyVideoPlayer.currentState == GSYVideoPlayer.CURRENT_STATE_PAUSE) {
                                                try {
                                                    gsyVideoPlayer.onVideoResume()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    gsyVideoPlayer.startAfterPrepared()
                                                    gsyVideoPlayer.startPlayLogic()
                                                }
                                            } else if (gsyVideoPlayer.currentState == GSYVideoPlayer.CURRENT_STATE_PREPAREING) {
                                                gsyVideoPlayer.startAfterPrepared()
                                                if(((seekPos.value*1000).toLong()+1000L)< totalTime.value){
                                                    println("seekTo:seekOnStart==>> ${seekPos.value}")
                                                    gsyVideoPlayer.seekOnStart = (seekPos.value*1000).toLong()
                                                }else{
                                                    gsyVideoPlayer.seekOnStart = 0
                                                }
                                            } else {
                                                gsyVideoPlayer.startPlayLogic()
                                            }
                                        } else {
                                            if(((seekPos.value*1000).toLong()+1000L)< totalTime.value ){
                                                println("seekTo:seekOnStart=22=>> ${seekPos.value}")
                                                gsyVideoPlayer.seekOnStart = (seekPos.value*1000).toLong()
                                            }else{
                                                gsyVideoPlayer.seekOnStart = 0
                                            }

                                            gsyVideoPlayer.startPlayLogic()
                                            isStarted.value = true
                                        }
                                    } else {
                                        if (gsyVideoPlayer.isInPlayingState) {
                                            gsyVideoPlayer.onVideoPause()
                                        }
                                    }
                                    isSliding.value=false
                                }
                            }
                        }
                }
            }
            onDispose {
                job0.value?.cancel()
            }
        }
        var job = remember { mutableStateOf<Job?>(null) }
        LaunchedEffect(source.value) {
            val source = source.value
            if (when (source) {
                    is VideoSource.File -> source.file.absolutePath.isNotEmpty()
                    is VideoSource.Url -> source.url.isNotEmpty()
                }
            ) {
                if (totalTime.value == 0) {
                    if (when (source) {
                            is VideoSource.File -> source.file.absolutePath.isNotEmpty()
                            is VideoSource.Url -> source.url.isNotEmpty()
                        }
                    ) {
                        job.value = launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    val retriever = MediaMetadataRetriever()
                                    retriever.setDataSource(
                                        when (source) {
                                            is VideoSource.File -> source.file.absolutePath
                                            is VideoSource.Url -> source.url
                                        }
                                    ) // 设置视频源


                                    val time =
                                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                    val duration = time!!.toLong()
                                    retriever.release()
                                    totalTime.value = duration.toInt()
                                } catch (e: Exception) {
                                }
                            }
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            GSYVideoManager.instance().setPlayerInitSuccessListener { iMediaPlayer, gsyModel ->
                gsyFuckingInitialized = iMediaPlayer
            }
        }
        DisposableEffect(Unit) {
            val simpleCall = object : GSYSampleCallBack() {
                override fun onPrepared(url: String?, vararg objects: Any?) {
                    try {
                        job.value?.cancel()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (totalTime.value == 0) {
                        totalTime.value = (gsyVideoPlayer.duration.coerceAtLeast(0).toInt())
                    }
                }

                override fun onPlayError(url: String?, vararg objects: Any?) {
                    println(
                        "播放isPlaying gsyVideoPlayer ==>> onPlayError ${
                            objects.joinToString { it.toString() }
                        }"
                    )
                    onAutomaticallyPause.invoke()
                }

                override fun onComplete(url: String?, vararg objects: Any?) {
                    val currentState = objects.mapNotNull {
                        it as? GSYPlayer
                    }.firstOrNull()?.currentState?:GSYVideoView.CURRENT_STATE_NORMAL
                   when(currentState){
                       GSYVideoView.CURRENT_STATE_AUTO_COMPLETE, GSYVideoView.CURRENT_STATE_ERROR -> {
                            isPause.value = true
                            fullscreen.value = false
                            gsyVideoPlayer.seekOnStart = 0
                            currentTime(0)
                            onAutomaticallyPause.invoke()
                          }
                   }
                }

                override fun onAutoComplete(url: String?, vararg objects: Any?) {
                    isPause.value = true
                    fullscreen.value = false
                    gsyVideoPlayer.seekOnStart = 0
                    currentTime(0)
                    onAutomaticallyPause.invoke()
                }
            }
            gsyVideoPlayer.setVideoAllCallBack(simpleCall)
            gsyVideoPlayer.setGSYVideoProgressListener { progress, secProgress, currentPosition, duration ->
                if (!isSliding.value) {
                    currentTime(currentPosition.coerceAtLeast(0).toInt())
                }
            }
            onDispose {
                onAutomaticallyPause.invoke()
                gsyVideoPlayer.gsyVideoManager.setListener(null)
                gsyVideoPlayer.setVideoAllCallBack(null)
            }
        }
    }

    val view = LocalView.current
    val windowInsetsController = remember(view) {
        WindowInsetsControllerCompat(context.getActivityWindow() ?: return@remember null, view)
    }
    LaunchedEffect(Unit) {
        snapshotFlow { fullscreen.value }
            .distinctUntilChanged()
            .collect {
                if (it) {
                    windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
                }
            }
    }
    return gsyVideoPlayer
}

actual typealias CMPPlayer = GSYPlayer

actual fun CMPPlayer.release() {
    this.release()
}

actual fun CMPPlayer.getCurrentFirstFrame(size: Size): ImageBitmap? {
    val retriever = FFmpegMediaMetadataRetriever()
    return try {
        retriever.setDataSource(this.getUrl())// 设置视频源
        val timeString =
            retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION) ?: "0"
        // 获取总长度
        val totalTime = timeString.toLongOrNull()?.times(1000) ?: 0
        var bmp: Bitmap? = null
        if (totalTime > 0) {
            // 这里为了实现简单，我们直接获取视频中间的画面
            bmp = retriever.getFrameAtTime(totalTime / 2, MediaMetadataRetriever.OPTION_CLOSEST)
        }
        bmp?.asImageBitmap() // 转换为 Compose 使用的 ImageBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        retriever.release()
    }
}

actual fun CMPPlayer.seekTo(position: Long) {
    if (position < 0) {
        return
    }
    try {
        if (this.gsyVideoManager.player.mediaPlayer != null) {
            this.gsyVideoManager.seekTo(position)
        }
    } catch (e: Exception) {
    }
}