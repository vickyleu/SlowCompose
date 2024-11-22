package chaintech.videoplayer.util

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import org.example.videoplayer.R
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class GSYPlayer:com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer {
    constructor(context: Context?, fullFlag: Boolean?) : super(context, fullFlag){
        IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT)
    }
    constructor(context: Context?) : super(context){
        IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT)
    }


    override fun getLayoutId(): Int {
        return R.layout.gsy_container
    }

    fun getUrl():String?{
        return mOriginUrl
    }

    override fun onVideoPause() {
        super.onVideoPause()
    }

    //TODO onLossTransientAudio 会导致播放器死锁
    override fun onLossTransientAudio() {
//        super.onLossTransientAudio()
    }
}