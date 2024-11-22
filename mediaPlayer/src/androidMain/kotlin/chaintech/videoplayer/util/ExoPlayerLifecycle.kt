package chaintech.videoplayer.util

import android.view.ViewGroup
import androidx.compose.runtime.MutableState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.exoplayer.ExoPlayer
import chaintech.videoplayer.ui.video.VideoSource
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

fun getExoPlayerLifecycleObserver(
    exoPlayer: ExoPlayer,
    isPause: Boolean,
    appInBackground: MutableState<Boolean>
): LifecycleEventObserver {
    return LifecycleEventObserver { source, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (appInBackground.value) {
                    exoPlayer.playWhenReady = !isPause
                }
            }

            Lifecycle.Event.ON_PAUSE -> {
                exoPlayer.playWhenReady = false
                appInBackground.value = true
            }

            Lifecycle.Event.ON_STOP -> {
                exoPlayer.playWhenReady = false
                appInBackground.value = true
            }

            Lifecycle.Event.ON_DESTROY -> {
                exoPlayer.release()
            }

            else -> Unit
        }
    }
}


fun getGsyPlayerLifecycleObserver(
    gsyVideoPlayer: StandardGSYVideoPlayer,
    isPause: MutableState<Boolean>,
    appInBackground: MutableState<Boolean>,
    onAutomaticallyPause: () -> Unit, source: VideoSource
): LifecycleEventObserver {
    return LifecycleEventObserver { source, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                appInBackground.value = false
                /*if(appInBackground.value){
                    if(!isPause){
                        gsyVideoPlayer.startPlayLogic()
//                        GSYVideoManager.instance().start()
                    }else{
                        gsyVideoPlayer.onVideoPause()
//                        GSYVideoManager.instance().pause()
                    }
                }*/
            }

            Lifecycle.Event.ON_PAUSE -> {
                gsyVideoPlayer.onVideoPause()
                isPause.value = true
                onAutomaticallyPause.invoke()
            }

            Lifecycle.Event.ON_STOP -> {
                appInBackground.value = true
                onAutomaticallyPause.invoke()
            }

            Lifecycle.Event.ON_DESTROY -> {
                gsyVideoPlayer.release()
                gsyVideoPlayer.parent?.let {
                    (it as ViewGroup).removeView(gsyVideoPlayer)
                }
                isPause.value = true
                onAutomaticallyPause.invoke()
            }

            else -> Unit
        }
    }
}