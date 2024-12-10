package org.uooc.compose.utils

import chaintech.videoplayer.ui.video.controls.VideoPlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.isActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VideoTimer(
    private val controller: VideoPlayerController,
    private val callback: (video_pos: Float, video_length: Float,counting:Int,markSuccess:()->Unit,reset:()->Unit) -> Unit
) {
    private var timerJob: Job? = null

    private var lastMarkedTime = 0L // 记录上次打点的时间

    // 倒计时Flow，每秒发射一个值
    private fun createTimerFlow(): Flow<Int> = flow {
        var i = 0 // 计数器
        while (currentCoroutineContext().isActive) {
            emit(i) // 每秒发射一次，表示经过1秒
            delay(1000) // 延迟1秒
            i++ // 计数器加1
        }
    }

    var counting=0

    fun markVideoPlaying(isPlaying: Boolean) {
        if (isPlaying) {
            // 开始计时，每秒检查一次
            if (timerJob == null) {
                timerJob = CoroutineScope(Dispatchers.IO).launch {
                    val currentTime_ = controller.currentPlayingTime.value
                    lastMarkedTime = currentTime_ // 更新打点时间
                    uploadProgress()
                    createTimerFlow()
                        .distinctUntilChanged()
                        .collect {
                            counting++
                            val currentTime = controller.currentPlayingTime.value
                            // 检查是否需要打点
                            if (currentTime - lastMarkedTime >= 30*1000L) {
                                lastMarkedTime = currentTime // 更新打点时间
                                uploadProgress()
                            }
                        }
                }
            }
        } else {
            // 暂停计时，停止打点
            timerJob?.cancel()
            val currentTime = controller.currentPlayingTime.value
            lastMarkedTime = currentTime // 更新打点时间
            uploadProgress()
            timerJob = null
        }
    }

    private fun uploadProgress() {
        val currentTime = controller.currentPlayingTime.value
        val duration = controller.duration.value
        println("上传学习进度::::${currentTime / 1000F} / ${duration / 1000F}")
        // 网络请求调用
        callback(currentTime / 1000F, duration / 1000F,counting,{
            counting=0
        }){
            lastMarkedTime = 0
        }
    }

    fun cancel() {
        counting=0
        timerJob?.cancel()
        timerJob = null
    }
}