package chaintech.videoplayer.util

import android.content.Context
import android.graphics.Color
import android.media.MediaCodecList
import android.net.Uri
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.SHOW_BUFFERING_ALWAYS
import chaintech.videoplayer.ui.video.VideoSource
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import java.net.URLDecoder


private fun codecCheck(selectedCodec: String): Boolean {
//
    // 使用 MediaCodecList 检查硬件解码器是否存在
    val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
    val codecs = codecList.codecInfos
    for (codec in codecs) {
        if (codec.isEncoder) continue
        if (codec.supportedTypes.any { it.equals("video/avc", ignoreCase = true) }) {
            if (codec.name.contains(selectedCodec, ignoreCase = true)) {
                return true
            }
            if (codec.name.contains("OMX", ignoreCase = true) && !codec.name.contains(
                    "google",
                    ignoreCase = true
                )
            ) {
                return true
            }
        }
    }
    return false
}

private fun checkHardwareDecoder(): Boolean {
    val otherCodec = "c2.android.avc.decoder"
    val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
    val codecs = codecList.codecInfos
    for (codec in codecs) {
        if (codec.isEncoder) continue
        if (codec.supportedTypes.any { it.equals("video/avc", ignoreCase = true) }) {
            if (codec.name.contains(otherCodec, ignoreCase = true)) {
                println("codec.name = ${codec.name}")
                return true
            }
            if (codec.name.contains("OMX", ignoreCase = true) && !codec.name.contains(
                    "google",
                    ignoreCase = true
                )
            ) {
                println("codec.name = ${codec.name}")
                return true
            }
        }
    }
    return false
}

@OptIn(UnstableApi::class)
@Composable
fun rememberExoPlayerWithLifecycle(
    reelUrl: String,
    context: Context,
    isPause: State<Boolean>
): ExoPlayer {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val exoPlayer = remember {
        // 创建一个强制硬件加速的自定义 RenderersFactory
        /*val renderersFactory: RenderersFactory =  NextRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            .setEnableDecoderFallback(true)
            // 强制开启硬件加速
            .setMediaCodecSelector(MediaCodecUtilImpl::getDecoderInfos)*/

        /* val renderersFactory: RenderersFactory = object : DefaultRenderersFactory(context) {
             override fun buildAudioRenderers(
                 context: Context,
                 extensionRendererMode: Int,
                 mediaCodecSelector: MediaCodecSelector,
                 enableDecoderFallback: Boolean,
                 audioSink: AudioSink,
                 eventHandler: Handler,
                 eventListener: AudioRendererEventListener,
                 out: java.util.ArrayList<Renderer>
             ) {
                 out.add(FfmpegAudioRenderer(eventHandler, eventListener,audioSink))
                 super.buildAudioRenderers(
                     context,
                     extensionRendererMode,
                     mediaCodecSelector,
                     enableDecoderFallback,
                     audioSink,
                     eventHandler,
                     eventListener,
                     out
                 )
             }
             override fun buildVideoRenderers(
                 context: Context,
                 extensionRendererMode: Int,
                 mediaCodecSelector: MediaCodecSelector,
                 enableDecoderFallback: Boolean,
                 eventHandler: Handler,
                 eventListener: VideoRendererEventListener,
                 allowedVideoJoiningTimeMs: Long,
                 out: ArrayList<Renderer>
             ) {

                 out.add(FfmpegVideoRenderer(allowedVideoJoiningTimeMs,eventHandler, eventListener, 10))
                 out.add(object : MediaCodecVideoRenderer(
                     context,
                     mediaCodecSelector,
                     allowedVideoJoiningTimeMs,
                     eventHandler,
                     eventListener,
                     MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY
                 ) {
                     override fun codecNeedsSetOutputSurfaceWorkaround(name: String): Boolean {
                         return codecCheck(name)
                     }
                 })
             }
         }
             .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
             .setAllowedVideoJoiningTimeMs(5000)
             .experimentalSetMediaCodecAsyncCryptoFlagEnabled(true)
             .setEnableAudioFloatOutput(true)
             .setEnableDecoderFallback(true)
             // 强制开启硬件加速
 //            .setMediaCodecSelector(MediaCodecUtilImpl::getDecoderInfos)*/
        ExoPlayer.Builder(context /*renderersFactory*/).build().apply {
            videoScalingMode = VIDEO_SCALING_MODE_SCALE_TO_FIT
//            videoScalingMode = VIDEO_SCALING_MODE_DEFAULT
            repeatMode = Player.REPEAT_MODE_ONE
            setHandleAudioBecomingNoisy(true)
        }
    }
    LaunchedEffect(reelUrl) {
        val videoUri = Uri.parse(reelUrl)
        val mediaItem = MediaItem.fromUri(videoUri)
        val mediaSource = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
            .createMediaSource(mediaItem)
        exoPlayer.seekTo(0, 0L)
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
    }
    val appInBackground = remember { mutableStateOf(false) }
    DisposableEffect(lifecycleOwner, appInBackground) {
        val observer =
            getExoPlayerLifecycleObserver(exoPlayer = exoPlayer, isPause.value, appInBackground)
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    return exoPlayer
}


@OptIn(UnstableApi::class)
@Composable
fun rememberGsyPlayerWithLifecycle(
    realSource: State<VideoSource>,
    forceUpdate: State<Boolean>,
    context: Context,
    isPause: MutableState<Boolean>,
    onAutomaticallyPause: () -> Unit, seekPos: State<Float>,totalTime: MutableState<Int>
): GSYPlayer {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val realSourceKey = remember(realSource.value) { mutableStateOf(realSource.value) }
    val scope = rememberCoroutineScope()
    val gsyPlayer = remember{
        /*val gsyManager = GSYVideoManager.instance().player
        val lastSourceUri =
            gsyManager?.mediaPlayer?.let {
                try {
                    URLDecoder.decode(it.dataSource, "utf-8")
                }catch (e:Exception){
                    null
                }
            }
        val source = realSource.value
        if (lastSourceUri != null && lastSourceUri.contains(
                when (source) {
                    is VideoSource.File -> source.file.absolutePath
                    is VideoSource.Url -> source.url
                }
            )
        ) {
            try {
                gsyManager.stop()
                gsyManager.mediaPlayer.release()
//                onAutomaticallyPause()
            } catch (e: Exception) {
            }
        }*/
        mutableStateOf<GSYPlayer>(
            GSYPlayer(context).apply {
                val player = this
                setIsTouchWiget(false) // 禁止触摸
                isReleaseWhenLossAudio = false; // 当音频焦点丢失时是否释放MediaPlayer
                val gsyVideoOptionBuilder = GSYVideoOptionBuilder()
                gsyVideoOptionBuilder
                    .setIsTouchWiget(false)
                    .setIsTouchWigetFull(false)
                    .setSoundTouch(true)
                    .build(player)

            })
    }

    LaunchedEffect(realSourceKey.value) {
        if(totalTime.value>0){
            gsyPlayer.value.onVideoReset()
            try {
                if (gsyPlayer.value.gsyVideoManager.player.mediaPlayer != null) {
                    val seekTo = if(((seekPos.value*1000L).toInt()+1000L)< totalTime.value) (seekPos.value*1000L).toLong()  else 0L
                    println("seekTo:seekOnStart=tttt=>> = $seekTo")
                    gsyPlayer.value.gsyVideoManager.seekTo(seekTo)
//                gsyPlayer.value.gsyVideoManager.start()
                }
            } catch (e: Exception) {
            }
            val seekTo = (if(((seekPos.value*1000L).toInt()+1000L)< totalTime.value) {
                (seekPos.value*1000L).toLong()
            } else 0L)
            println("seekTo:seekOnStart=ttttaaaaa=>> = $seekTo")
            gsyPlayer.value.seekOnStart = seekTo
        }
        val source = realSourceKey.value
        when (source) {
            is VideoSource.File -> gsyPlayer.value.setUp("${source.file.absolutePath}", false, "测试视频")
            is VideoSource.Url -> gsyPlayer.value.setUp(source.url, true, "测试视频")
        }
    }

    val appInBackground = remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val observer =
            getGsyPlayerLifecycleObserver(
                gsyVideoPlayer = gsyPlayer.value,
                isPause, appInBackground, onAutomaticallyPause, realSourceKey.value
            )
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    return gsyPlayer.value
}

class NoTouchFrameLayout(context: Context) : FrameLayout(context) {
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        // 拦截所有触摸事件，返回 true 不向下传递事件
        return true
    }

}


@OptIn(UnstableApi::class)
@Composable
fun rememberPlayerView(exoPlayer: ExoPlayer, context: Context): PlayerView {
    val player = remember {
        PlayerView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
//            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            player = exoPlayer
            setShowBuffering(SHOW_BUFFERING_ALWAYS)
            setShutterBackgroundColor(Color.TRANSPARENT)

            setKeepContentOnPlayerReset(true)

            if (layoutParams is FrameLayout.LayoutParams) {
                (layoutParams as FrameLayout.LayoutParams).gravity = Gravity.CENTER
            } else {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            }
//            // 手动设置对齐方式
//            (layoutParams as? FrameLayout.LayoutParams)?.gravity = Gravity.CENTER
            //            LAYER_TYPE_NONE,
            //            LAYER_TYPE_SOFTWARE,
            //            LAYER_TYPE_HARDWARE
//            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            setLayerType(View.LAYER_TYPE_HARDWARE, null)
//            if(checkHardwareDecoder()){
//
//            }else{
//                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player.player = null
        }
    }

    return player
}