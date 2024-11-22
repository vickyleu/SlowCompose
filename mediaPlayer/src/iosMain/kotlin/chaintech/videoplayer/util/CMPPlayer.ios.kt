@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package chaintech.videoplayer.util

import MediaObserver.MediaObserverProtocol
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import chaintech.videoplayer.model.PlayerSpeed
import chaintech.videoplayer.ui.video.VideoSource
import com.github.jing332.filepicker.base.FileImpl
import com.github.jing332.filepicker.base.absolutePath
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVAssetImageGenerator
import platform.AVFoundation.AVKeyValueStatusLoaded
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemFailedToPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatus
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.AVPlayerTimeControlStatusPaused
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.AVFoundation.AVQueuePlayer
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.asset
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.isPlaybackBufferEmpty
import platform.AVFoundation.muted
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.playbackLikelyToKeepUp
import platform.AVFoundation.rate
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.seekToTime
import platform.AVFoundation.setRate
import platform.AVFoundation.timeControlStatus
import platform.AVKit.AVPlayerViewController
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.CGDataProviderCopyData
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetAlphaInfo
import platform.CoreGraphics.CGImageGetBytesPerRow
import platform.CoreGraphics.CGImageGetDataProvider
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.NSLog
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLFragmentAllowedCharacterSet
import platform.Foundation.URLPathAllowedCharacterSet
import platform.Foundation.addObserver
import platform.Foundation.create
import platform.Foundation.didChangeValueForKey
import platform.Foundation.removeObserver
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.stringByAppendingString
import platform.Foundation.willChangeValueForKey
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIImage
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import what.the.fuck.with.vimediacache.VIResourceLoaderManager
import what.the.fuck.with.vimediacache.playerItemWithURL


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
fun CMPPlayerCompos2(
    modifier: Modifier,
    source: State<VideoSource>,
    forceUpdate: State<Boolean>,
    isPause: MutableState<Boolean>,
    isMute: State<Boolean>,
    isLooping: Boolean,
    totalTime: MutableState<Int>,
    seekPos: MutableState<Float>,
    currentTime: ((Int) -> Unit),
    onAutomaticallyPause: () -> Unit,
    isSliding: MutableState<Boolean>,
    sliderTime: MutableState<Int?>,
    speed: MutableState<PlayerSpeed>,
    fullscreen: MutableState<Boolean>
): CMPPlayer {
    val resourceLoaderManager = remember { VIResourceLoaderManager() }
    val playerItem = remember { mutableStateOf<AVPlayerItem?>(null) }
    val player: AVQueuePlayer by remember { mutableStateOf(AVQueuePlayer(playerItem.value)) }
    val playerLayer by remember { mutableStateOf(AVPlayerLayer()) }
    val avPlayerViewController = remember {
        AVPlayerViewController().apply {
            this.player = player
            this.showsPlaybackControls = false
            this.videoGravity = AVLayerVideoGravityResizeAspectFill
        }
    }

    val playerContainer = remember {
        UIView().apply {
            layer.addSublayer(playerLayer)
        }
    }

    LaunchedEffect(source.value) {
        snapshotFlow { isMute.value }
            .distinctUntilChanged()
            .collect { player.muted = it }
    }

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    fun setPlayerRate(speed: PlayerSpeed) {
        player.rate = when (speed) {
            PlayerSpeed.X0_5 -> 0.5f
            PlayerSpeed.X1 -> 1f
            PlayerSpeed.X1_5 -> 1.5f
            PlayerSpeed.X2 -> 2f
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { speed.value }
            .distinctUntilChanged()
            .collect {
                setPlayerRate(it)
            }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { totalTime.value }
            .distinctUntilChanged()
            .filter { isPause.value.not() && seekPos.value > 0 }
            .collect {
                val seekTo = if (((seekPos.value * 1000L).toLong() + 1000L) < totalTime.value) {
                    (seekPos.value * 1000L).toLong()
                } else 0L
                player.seekTo(seekTo)
            }
    }

    val observerObject = remember {
        object : NSObject(), MediaObserverProtocol {
            private val current = this

            private var isRegisterEvent = false
            fun addPlayerEventObserver(player: AVPlayer) {
//                player.addObserver(
//                    current, forKeyPath = "rate",
//                    options = NSKeyValueObservingOptionNew, context = null
//                )
//                player.addObserver(
//                    current, forKeyPath = "timeControlStatus",
//                    options = NSKeyValueObservingOptionNew, context = null
//                )
                if (isRegisterEvent) return
                NSNotificationCenter.defaultCenter().addObserver(
                    current,
                    NSSelectorFromString(current::onPlayerItemDidPlayToEndTime.name),
                    AVPlayerItemDidPlayToEndTimeNotification,
                    player.currentItem
                )
                isRegisterEvent = true
            }

            fun removePlayerEventObserver(player: AVPlayer) {
                if (isRegisterEvent.not()) return
                NSNotificationCenter.defaultCenter().removeObserver(current)
                isRegisterEvent = false
            }

            private var isRegister = false


            override fun observeValueForKeyPath(
                keyPath: String?,
                ofObject: Any?,
                change: Map<Any?, *>?,
                context: COpaquePointer?
            ) {
                if (keyPath == "playbackBufferEmpty") {
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            isLoading = player.currentItem?.isPlaybackBufferEmpty() ?: false
                        }
                    }
                } else if (keyPath == "playbackLikelyToKeepUp") {
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            isLoading = player.currentItem?.playbackLikelyToKeepUp?.not() ?: false
                        }
                    }
                }
            }

            fun observePlayerItemBufferStatus(playerItem: AVPlayerItem?) {
                if (playerItem == null || isRegister) {
                    return
                }
                playerItem.addObserver(
                    current, forKeyPath = "playbackBufferEmpty",
                    options = NSKeyValueObservingOptionNew, context = null
                )
                playerItem.addObserver(
                    current, forKeyPath = "playbackLikelyToKeepUp",
                    options = NSKeyValueObservingOptionNew, context = null
                )
            }

            fun removeObserverPlayerItemBufferStatus(playerItem: AVPlayerItem?) {
                if (playerItem == null) {
                    isRegister = false
                    return
                }
                if (isRegister.not()) return
                playerItem.removeObserver(this, forKeyPath = "playbackBufferEmpty")
                playerItem.removeObserver(this, forKeyPath = "playbackLikelyToKeepUp")
                isRegister = false
            }

            @Suppress("unused")
            @ObjCAction
            fun onPlayerItemDidPlayToEndTime() {
                player.currentItem?.let { item ->
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            if (isLooping) {
                                delay(100)
                                avPlayerViewController.player = null

                                player.safeRemoveAllItems()
                                if (player.canInsertItem(item, afterItem = null)) {
                                    player.insertItem(item, afterItem = null)
                                }

                                avPlayerViewController.player = player // 重新关联播放器
                                player.seekToTime(CMTimeMakeWithSeconds(0.0, 1))
                                player.playVideo(speed.value)
                                addPlayerEventObserver(player)
                            } else {
                                fullscreen.value = false
                                isPause.value = true
                                onAutomaticallyPause()
                                avPlayerViewController.player = null

                                player.safeRemoveAllItems()
                                if (player.canInsertItem(item, afterItem = null)) {
                                    player.insertItem(item, afterItem = null)
                                }

                                avPlayerViewController.player = player // 重新关联播放器
                                currentTime(0)
                                player.seekToTime(CMTimeMakeWithSeconds(0.0, 1))
                                addPlayerEventObserver(player)
                            }
                        }
                    }
                    removePlayerEventObserver(player)
                }

            }
        }
    }
    val timeObserver = remember { mutableStateOf<Any?>(null) }
    val timeObserverJob = remember { mutableStateOf<Job?>(null) }
    DisposableEffect(Unit) {
        currentTime(0)
        isLoading = true
        observerObject.addPlayerEventObserver(player)
        onDispose {
            player.pauseVideo()
            observerObject.removePlayerEventObserver(player)
            observerObject.removeObserverPlayerItemBufferStatus(playerItem.value)
            playerItem.value = null
            timeObserverJob.value?.cancel()
            player.safeRemoveAllItems()
        }
    }

    DisposableEffect(source.value) {
        timeObserverJob.value?.cancel()
        timeObserverJob.value = scope.launch {
            val coroutineScope = this
            withContext(Dispatchers.IO) {
                delay(200)
                withContext(Dispatchers.Main) {
                    timeObserver.value = player.addPeriodicTimeObserverForInterval(
                        CMTimeMakeWithSeconds(1.0, 1),
                        dispatch_get_main_queue()
                    ) { _ ->
                        if (player.rate == 0f) {
                            if (player.currentItem != null) {
                                when (player.currentItem!!.status) {
                                    AVPlayerItemStatusReadyToPlay -> {
                                        if (isPause.value.not()) {
                                            val seekTo =
                                                if (((seekPos.value * 1000L).toLong() + 1000L) < totalTime.value) {
                                                    (seekPos.value * 1000L).toLong()
                                                } else 0L
                                            player.seekTo(seekTo)
                                            player.playVideo(speed.value)
                                        }
                                    }

                                    AVPlayerItemStatusFailed -> {
                                        isPause.value = true
                                        onAutomaticallyPause()
                                        val current =
                                            (CMTimeGetMilliseconds(player.currentTime()))
                                                .coerceAtLeast(0.0).toInt()
                                        currentTime(current)
                                    }

                                    else -> Unit
                                }
                            } else {
                                isPause.value = true
                                onAutomaticallyPause()
                            }
                        }

                        if (!isSliding.value) {
                            scope.launch {
                                withContext(Dispatchers.Main) {
                                    if (!isPause.value) {
                                        val duration =
                                            (player.currentItem?.duration?.let {
                                                CMTimeGetMilliseconds(
                                                    it
                                                )
                                            } ?: 0.0).coerceAtLeast(0.0)
                                        val current =
                                            (CMTimeGetMilliseconds(player.currentTime()))
                                                .coerceAtLeast(0.0).toInt()
                                        currentTime(current)
                                        if (totalTime.value == 0) {
                                            totalTime.value = (duration.toInt())
                                        }
                                        isLoading =
                                            player.currentItem?.playbackLikelyToKeepUp?.not()
                                                ?: false
                                    } else {
                                        isLoading = false
                                        val current =
                                            (CMTimeGetMilliseconds(player.currentTime()))
                                                .coerceAtLeast(0.0).toInt()
                                        currentTime(current)
                                        if (current == 0) {
                                            onAutomaticallyPause()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        onDispose {
            observerObject.removeObserverPlayerItemBufferStatus(playerItem.value)
            playerItem.value = null
            if (timeObserver.value != null) {
                try {
                    avPlayerViewController.player = null
                    player.removeTimeObserver(timeObserver.value!!)
                    timeObserver.value = null
                    player.pause()
                } catch (e: Exception) {
                    println("last removeTimeObserver error: ${e.message}")
                }
            }
        }
    }

    LaunchedEffect(source.value) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val videoSource = source.value
                val newItem = when (videoSource) {
                    is VideoSource.File -> {
                        val exist = NSFileManager.defaultManager()
                            .fileExistsAtPath(videoSource.file.absolutePath)
                        if (exist) {
                            val localURL = NSURL.toSafeUrl(videoSource.file)
                            println("文件存在:localURL:${localURL?.absoluteString}")
                            val asset = localURL?.let {
                                AVAsset.assetWithURL(it)
                            }
                            asset?.let {
                                it.loadValuesAsynchronouslyForKeys(listOf("duration")) {
                                    memScoped {
                                        val nsErrorPtr = nativeHeap.alloc<ObjCObjectVar<NSError?>>()
                                        val status =
                                            asset.statusOfValueForKey("duration", nsErrorPtr.ptr)
                                        if (status == AVKeyValueStatusLoaded) {
                                            val duration = asset.duration
                                            val seconds = (CMTimeGetMilliseconds(duration))
                                            totalTime.value = seconds.toInt()
                                        } else {
                                            NSLog(
                                                "Failed to load duration: error=%@  absoluteString=${localURL.absoluteString}",
                                                nsErrorPtr.value
                                            )
                                            println("Failed to load duration: error=${nsErrorPtr.value?.toString()}  absoluteString=${localURL.absoluteString}")
                                        }
                                    }
                                }
                                // 使用 VIResourceLoaderManager 提供的 playerItem
                                AVPlayerItem.playerItemWithAsset(it)
                            }
                        } else {
                            null
                        }
                    }

                    is VideoSource.Url -> {
                        val encodedUrl = NSString.create(string = videoSource.url)
                            .stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLFragmentAllowedCharacterSet)
                        val urlObject = NSURL.URLWithString(URLString = encodedUrl ?: "")
                        if (urlObject != null) {
                            val asset = AVURLAsset.assetWithURL(urlObject)
//                            val asset = AVAsset.assetWithURL(urlObject)
                            asset.loadValuesAsynchronouslyForKeys(listOf("duration")) {
                                val duration = asset.duration
                                val milliseconds = CMTimeGetMilliseconds(duration)
                                totalTime.value = milliseconds.toInt()
                            }
                        }
                        // 使用 VIResourceLoaderManager 提供的 playerItem
                        urlObject?.let { resourceLoaderManager.playerItemWithURL(it) }
                    }
                }

                avPlayerViewController.player = null

                playerItem.value = null
                playerItem.value = newItem
                player.safeRemoveAllItems()
                if (newItem != null) {
                    player.insertItem(newItem, afterItem = null)
                }
                avPlayerViewController.player = player // 重新关联播放器
                currentTime(0)
                observerObject.observePlayerItemBufferStatus(newItem)
                withContext(Dispatchers.Main) {
                    if (totalTime.value > 0) {
                        val seekTo =
                            if (((seekPos.value * 1000L).toLong() + 1000L) < totalTime.value) {
                                (seekPos.value * 1000L).toLong()
                            } else 0L
                        player.seekTo(seekTo)
                    }
                    setPlayerRate(speed.value)
                    if (isPause.value) {
                        player.pauseVideo()
                    } else {
                        player.playVideo(speed.value)
                        setPlayerRate(speed.value)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { isPause.value }
            .distinctUntilChanged()
            .collect {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        if (it) {
                            player.pauseVideo()
                        } else {
                            player.playVideo(speed.value)
                            setPlayerRate(speed.value)
                        }
                    }
                }
            }
    }

    val frame = remember { mutableStateOf(Size.Zero) }
    LaunchedEffect(Unit) {
        snapshotFlow { frame.value }
            .distinctUntilChanged()
            .collect {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        CATransaction.begin()
                        CATransaction.setValue(true, kCATransactionDisableActions)
                        val rect = CGRectMake(0.0, 0.0, it.width.toDouble(), it.height.toDouble())
                        playerLayer.setFrame(rect)
                        playerContainer.setFrame(rect)
                        avPlayerViewController.view.layer.frame = rect
                        CATransaction.commit()
                    }
                }
            }
    }

    with(LocalDensity.current) {
        BoxWithConstraints(modifier = modifier.then(Modifier.onGloballyPositioned {
            frame.value = it.size.toSize()
        })) {
            UIKitView(
                factory = {
                    playerContainer.addSubview(avPlayerViewController.view)
                    playerContainer.setFrame(
                        CGRectMake(
                            0.0,
                            0.0,
                            frame.value.width.toDouble(),
                            frame.value.height.toDouble()
                        )
                    )

                    // 禁用 autoresizing mask
                    avPlayerViewController.view.translatesAutoresizingMaskIntoConstraints = false
                    // 设置 Auto Layout 约束
                    NSLayoutConstraint.activateConstraints(
                        listOf(
                            avPlayerViewController.view.leadingAnchor.constraintEqualToAnchor(
                                playerContainer.leadingAnchor
                            ),
                            avPlayerViewController.view.trailingAnchor.constraintEqualToAnchor(
                                playerContainer.trailingAnchor
                            ),
                            avPlayerViewController.view.topAnchor.constraintEqualToAnchor(
                                playerContainer.topAnchor
                            ),
                            avPlayerViewController.view.bottomAnchor.constraintEqualToAnchor(
                                playerContainer.bottomAnchor
                            )
                        )
                    )
                    playerContainer
                },
                modifier = modifier.then(Modifier.onGloballyPositioned {
                    val rect = it.boundsInParent().let {
                        CGRectMake(
                            0.0,
                            0.0,
                            it.width.toDp().value.toDouble(),
                            it.height.toDp().value.toDouble()
                        )
                    }
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            CATransaction.begin()
                            CATransaction.setValue(true, kCATransactionDisableActions)
                            playerContainer.layer.setFrame(rect)
                            playerLayer.setFrame(rect)
                            playerContainer.setFrame(rect)
                            avPlayerViewController.view.layer.frame = rect
                            CATransaction.commit()
                        }
                    }
                }),
                update = { view ->
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            playerContainer.setFrame(
                                CGRectMake(
                                    0.0,
                                    0.0,
                                    frame.value.width.toDouble(),
                                    frame.value.height.toDouble()
                                )
                            )
                            if (isPause.value) {
                                player.pauseVideo()
                            } else {
                                player.playVideo(speed.value)
                            }
                            sliderTime.value?.let {
                                val time = CMTimeMake(value = it.toLong(), timescale = 1000)
                                player.seekToTime(time)
                            }
                        }
                    }
                },
                properties = UIKitInteropProperties(
                    isInteractive = false,
                    isNativeAccessibilityEnabled = true
                )
            )
            LaunchedEffect(Unit) {
                snapshotFlow { sliderTime.value }
                    .filterNotNull()
                    .distinctUntilChanged()
                    .collect {
                        scope.launch {
                            withContext(Dispatchers.Main) {
                                sliderTime.value?.let {
                                    val time = CMTimeMake(value = it.toLong(), timescale = 1000)
                                    player.seekToTime(time)
                                }
                            }
                        }
                    }
            }
        }
    }

    return player
}

interface CMPlayerEventListener {
    fun onPlayerState(owner: VideoSafeObserver,state:AVPlayerItemStatus?)
    fun onPlayerItemDidPlayToEndTime(owner: VideoSafeObserver)
    fun onPlayerFailedToPlay(owner: VideoSafeObserver)
    fun onPlayerBuffering(owner: VideoSafeObserver)
    fun onPlayerBufferingCompleted(owner: VideoSafeObserver)
    fun onPlayerPlaying(owner: VideoSafeObserver)
    fun onPlayerPaused(owner: VideoSafeObserver)
    fun onPlaying(owner: VideoSafeObserver, pos: Long, dur: Long) // 播放进度回调
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
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
    currentTime: ((Int) -> Unit),
    onAutomaticallyPause: () -> Unit,
    isSliding: MutableState<Boolean>,
    sliderTime: MutableState<Int?>,
    speed: MutableState<PlayerSpeed>,
    fullscreen: MutableState<Boolean>
): CMPPlayer {
    val resourceLoaderManager = remember { VIResourceLoaderManager() }
    val playerItem = remember { mutableStateOf<AVPlayerItem?>(null) }
    val player: AVQueuePlayer by remember { mutableStateOf(AVQueuePlayer(playerItem.value)) }
    val playerLayer by remember { mutableStateOf(AVPlayerLayer()) }
    val avPlayerViewController = remember {
        AVPlayerViewController().apply {
            this.player = player
            this.showsPlaybackControls = false
            this.videoGravity = AVLayerVideoGravityResizeAspectFill
        }
    }

    val playerContainer = remember {
        UIView().apply {
            layer.addSublayer(playerLayer)
        }
    }

    LaunchedEffect(source.value) {
        snapshotFlow { isMute.value }
            .distinctUntilChanged()
            .collect { player.muted = it }
    }

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    fun setPlayerRate(speed: PlayerSpeed) {
        player.rate = when (speed) {
            PlayerSpeed.X0_5 -> 0.5f
            PlayerSpeed.X1 -> 1f
            PlayerSpeed.X1_5 -> 1.5f
            PlayerSpeed.X2 -> 2f
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { speed.value }
            .distinctUntilChanged()
            .collect {
                setPlayerRate(it)
            }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { totalTime.value }
            .distinctUntilChanged()
            .filter { isPause.value.not() && seekPos.value > 0 }
            .collect {
                val seekTo = if (((seekPos.value * 1000L).toLong() + 1000L) < totalTime.value) {
                    (seekPos.value * 1000L).toLong()
                } else 0L
                player.seekTo(seekTo)
            }
    }

    val observerObject = remember {
        VideoSafeObserver(object : CMPlayerEventListener {
            override fun onPlayerState(owner: VideoSafeObserver, state: AVPlayerItemStatus?) {
                if(player.currentItem != null){
                    when(state){
                        AVPlayerItemStatusReadyToPlay -> {
                            if (isPause.value.not()) {
                                val seekTo =
                                    if (((seekPos.value * 1000L).toLong() + 1000L) < totalTime.value) {
                                        (seekPos.value * 1000L).toLong()
                                    } else 0L
                                println("seekTo:$seekTo")
                                player.seekTo(seekTo)
                                player.playVideo(speed.value)
                            }
                        }
                        AVPlayerItemStatusFailed -> {
                            isPause.value = true
                            onAutomaticallyPause()
                            val current =
                                (CMTimeGetMilliseconds(player.currentTime()))
                                    .coerceAtLeast(0.0).toInt()
                            currentTime(current)
                        }
                        else -> Unit
                    }
                }else{
                    isPause.value = true
                    onAutomaticallyPause()
                }
            }
            override fun onPlayerItemDidPlayToEndTime(owner: VideoSafeObserver) {
                owner.removePlayerItemEventObserver(player.currentItem)
                println("onPlayerItemDidPlayToEndTime 播放结束了 currentItem?=${player.currentItem!=null}")
                player.currentItem?.let { item ->
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            if (isLooping) {// 循环播放
                                player.safeRemoveAllItems()
                                if (player.canInsertItem(item, afterItem = null)) {
                                    player.insertItem(item, afterItem = null)
                                }
                                player.seekToTime(CMTimeMakeWithSeconds(0.0, 1))
                                player.playVideo(speed.value)
                                owner.addPlayerItemEventObserver(item)
                            } else {
                                fullscreen.value = false
                                isPause.value = true
                                onAutomaticallyPause()

                                player.safeRemoveAllItems()
                                if (player.canInsertItem(item, afterItem = null)) {
                                    player.insertItem(item, afterItem = null)
                                }
                                currentTime(0)
                                println("onPlayerItemDidPlayToEndTime 播放结束了")
                                player.seekToTime(CMTimeMakeWithSeconds(0.0, 1))
                                owner.addPlayerItemEventObserver(item)
                            }
                        }
                    }
                }
            }

            override fun onPlayerFailedToPlay(owner: VideoSafeObserver) {
                owner.removePlayerItemEventObserver(player.currentItem)
                isPause.value = true
                onAutomaticallyPause()
            }

            override fun onPlayerBuffering(owner: VideoSafeObserver) {
                if (isPause.value.not()) {
                    isLoading = true
                }
            }

            override fun onPlayerBufferingCompleted(owner: VideoSafeObserver) {
                if (isPause.value.not()) {
                    isLoading = false
                }
            }

            override fun onPlayerPlaying(owner: VideoSafeObserver) {

            }

            override fun onPlayerPaused(owner: VideoSafeObserver) {
                isPause.value = true
            }

            override fun onPlaying(owner: VideoSafeObserver, pos: Long, dur: Long) {
                if (!isSliding.value) {
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            if (!isPause.value) {
                                println("onPlaying 播放结束了pos:$pos")

                                currentTime(pos.toInt())
                                if (totalTime.value == 0) {
                                    totalTime.value = (dur.toInt())
                                }
                                isLoading =
                                    player.currentItem?.playbackLikelyToKeepUp?.not()
                                        ?: false
                            } else {
                                isLoading = false
                                val current =
                                    (CMTimeGetMilliseconds(player.currentTime()))
                                        .coerceAtLeast(0.0).toInt()
                                currentTime(current)
                                if (current == 0) {
                                    onAutomaticallyPause()
                                }
                            }
                        }
                    }
                }
            }

        })
    }
    DisposableEffect(Unit) {
        currentTime(0)
        isLoading = true
        observerObject.addPlayerEventObserver(player)
        onDispose {
            player.pauseVideo()
            observerObject.removePlayerEventObserver(player)
            observerObject.removePlayerItemEventObserver(playerItem.value)
            playerItem.value = null
            player.safeRemoveAllItems()
        }
    }


    LaunchedEffect(source.value) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val videoSource = source.value
                val newItem = when (videoSource) {
                    is VideoSource.File -> {
                        val exist = NSFileManager.defaultManager()
                            .fileExistsAtPath(videoSource.file.absolutePath)
                        if (exist) {
                            val localURL = NSURL.toSafeUrl(videoSource.file)
                            println("文件存在:localURL:${localURL?.absoluteString}")
                            val asset = localURL?.let {
                                AVAsset.assetWithURL(it)
                            }
                            asset?.let {
                                it.loadValuesAsynchronouslyForKeys(listOf("duration")) {
                                    memScoped {
                                        val nsErrorPtr = nativeHeap.alloc<ObjCObjectVar<NSError?>>()
                                        val status =
                                            asset.statusOfValueForKey("duration", nsErrorPtr.ptr)
                                        if (status == AVKeyValueStatusLoaded) {
                                            val duration = asset.duration
                                            val seconds = (CMTimeGetMilliseconds(duration))
                                            totalTime.value = seconds.toInt()
                                        } else {
                                            NSLog(
                                                "Failed to load duration: error=%@  absoluteString=${localURL.absoluteString}",
                                                nsErrorPtr.value
                                            )
                                            println("Failed to load duration: error=${nsErrorPtr.value?.toString()}  absoluteString=${localURL.absoluteString}")
                                        }
                                    }
                                }
                                // 使用 VIResourceLoaderManager 提供的 playerItem
                                AVPlayerItem.playerItemWithAsset(it)
                            }
                        } else {
                            null
                        }
                    }

                    is VideoSource.Url -> {
                        val encodedUrl = NSString.create(string = videoSource.url)
                            .stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLFragmentAllowedCharacterSet)
                        val urlObject = NSURL.URLWithString(URLString = encodedUrl ?: "")
                        if (urlObject != null) {
                            val asset = AVURLAsset.assetWithURL(urlObject)
//                            val asset = AVAsset.assetWithURL(urlObject)
                            asset.loadValuesAsynchronouslyForKeys(listOf("duration")) {
                                val duration = asset.duration
                                val milliseconds = CMTimeGetMilliseconds(duration)
                                totalTime.value = milliseconds.toInt()
                            }
                        }
                        // 使用 VIResourceLoaderManager 提供的 playerItem
                        urlObject?.let { resourceLoaderManager.playerItemWithURL(it) }
                    }
                }
                playerItem.value = null
                playerItem.value = newItem
                player.safeRemoveAllItems()
                if (newItem != null) {
                    player.insertItem(newItem, afterItem = null)
                }
                currentTime(0)
                observerObject.addPlayerItemEventObserver(newItem)
                withContext(Dispatchers.Main) {
                    if (totalTime.value > 0) {
                        val seekTo =
                            if (((seekPos.value * 1000L).toLong() + 1000L) < totalTime.value) {
                                (seekPos.value * 1000L).toLong()
                            } else 0L
                        player.seekTo(seekTo)
                    }
                    setPlayerRate(speed.value)
                    if (isPause.value) {
                        player.pauseVideo()
                    } else {
                        player.playVideo(speed.value)
                        setPlayerRate(speed.value)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { isPause.value }
            .distinctUntilChanged()
            .collect {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        if (it) {
                            player.pauseVideo()
                        } else {
                            player.playVideo(speed.value)
                            setPlayerRate(speed.value)
                        }
                    }
                }
            }
    }

    val frame = remember { mutableStateOf(Size.Zero) }
    LaunchedEffect(Unit) {
        snapshotFlow { frame.value }
            .distinctUntilChanged()
            .collect {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        CATransaction.begin()
                        CATransaction.setValue(true, kCATransactionDisableActions)
                        val rect = CGRectMake(0.0, 0.0, it.width.toDouble(), it.height.toDouble())
                        playerLayer.setFrame(rect)
                        playerContainer.setFrame(rect)
                        avPlayerViewController.view.layer.frame = rect
                        CATransaction.commit()
                    }
                }
            }
    }

    with(LocalDensity.current) {
        BoxWithConstraints(
            modifier = modifier.then(Modifier.onGloballyPositioned {
                frame.value = it.size.toSize().let {
                    Size(it.width.toDp().value, it.height.toDp().value)
                }
            })
        ) {
            UIKitView(
                factory = {
                    playerContainer.addSubview(avPlayerViewController.view)
                    playerContainer.setFrame(
                        CGRectMake(
                            0.0,
                            0.0,
                            frame.value.width.toDouble(),
                            frame.value.height.toDouble()
                        )
                    )

                    // 禁用 autoresizing mask
                    avPlayerViewController.view.translatesAutoresizingMaskIntoConstraints = false
                    // 设置 Auto Layout 约束
                    NSLayoutConstraint.activateConstraints(
                        listOf(
                            avPlayerViewController.view.leadingAnchor.constraintEqualToAnchor(
                                playerContainer.leadingAnchor
                            ),
                            avPlayerViewController.view.trailingAnchor.constraintEqualToAnchor(
                                playerContainer.trailingAnchor
                            ),
                            avPlayerViewController.view.topAnchor.constraintEqualToAnchor(
                                playerContainer.topAnchor
                            ),
                            avPlayerViewController.view.bottomAnchor.constraintEqualToAnchor(
                                playerContainer.bottomAnchor
                            )
                        )
                    )
                    playerContainer
                },
                modifier = modifier.then(Modifier.onGloballyPositioned {
                    /*val rect = it.boundsInParent().let {
                        CGRectMake(
                            0.0,
                            0.0,
                            it.width.toDp().value.toDouble(),
                            it.height.toDp().value.toDouble()
                        )
                    }
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            CATransaction.begin()
                            CATransaction.setValue(true, kCATransactionDisableActions)
                            playerContainer.layer.setFrame(rect)
                            playerLayer.setFrame(rect)
                            playerContainer.setFrame(rect)
                            avPlayerViewController.view.layer.frame = rect
                            CATransaction.commit()
                        }
                    }*/
                }),
                update = { view ->
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            /*playerContainer.setFrame(
                                CGRectMake(
                                    0.0,
                                    0.0,
                                    frame.value.width.toDouble(),
                                    frame.value.height.toDouble()
                                )
                            )*/
                            if (isPause.value) {
                                player.pauseVideo()
                            } else {
                                player.playVideo(speed.value)
                            }
                            sliderTime.value?.let {
                                val time = CMTimeMake(value = it.toLong(), timescale = 1000)
                                player.seekToTime(time)
                            }
                        }
                    }
                },
                properties = UIKitInteropProperties(
                    isInteractive = false,
                    isNativeAccessibilityEnabled = true
                )
            )
            LaunchedEffect(Unit) {
                snapshotFlow { sliderTime.value }
                    .filterNotNull()
                    .distinctUntilChanged()
                    .collect {
                        scope.launch {
                            withContext(Dispatchers.Main) {
                                sliderTime.value?.let {
                                    val time = CMTimeMake(value = it.toLong(), timescale = 1000)
                                    player.seekToTime(time)
                                }
                            }
                        }
                    }
            }
        }
    }

    return player
}

class VideoSafeObserver(private val listener: CMPlayerEventListener) : NSObject(),
    MediaObserverProtocol {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun observeValueForKeyPath(
        keyPath: String?,
        ofObject: Any?,
        change: Map<Any?, *>?,
        context: COpaquePointer?
    ) {
        if (keyPath == null) return
        when (keyPath) {
            "rate" -> {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        if ((ofObject as? AVPlayer)?.isPlaying == true) {
                            listener.onPlayerPlaying(this@VideoSafeObserver)
                        } else {
                            listener.onPlayerPaused(this@VideoSafeObserver)
                        }
                    }
                }
            }

            "timeControlStatus" -> {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        val status = (ofObject as? AVPlayer)?.timeControlStatus
                        when (status) {
                            AVPlayerTimeControlStatusPaused -> listener.onPlayerPaused(this@VideoSafeObserver)
                            AVPlayerTimeControlStatusPlaying -> listener.onPlayerPlaying(this@VideoSafeObserver)
                            AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate -> listener.onPlayerBufferingCompleted(
                                this@VideoSafeObserver
                            )

                            else -> Unit
                        }
                    }
                }
            }

            "playbackBufferEmpty" -> listener.onPlayerBuffering(this@VideoSafeObserver)
            "playbackLikelyToKeepUp", "playbackBufferFull" -> listener.onPlayerBufferingCompleted(
                this@VideoSafeObserver
            )

            "status" -> {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        val itemStatus = (ofObject as? AVPlayerItem)?.status
                        when (itemStatus) {
                            AVPlayerItemStatusFailed -> listener.onPlayerFailedToPlay(this@VideoSafeObserver)
                            else -> Unit
                        }
                    }
                }
            }

            else -> Unit
        }
    }

    private var isRegisterEvent = false
    fun addPlayerEventObserver(player: AVPlayer) {
        if (isRegisterEvent) return
        player.addObserver(
            this, forKeyPath = "rate",
            options = NSKeyValueObservingOptionNew, context = null
        )
        player.addObserver(
            this, forKeyPath = "timeControlStatus",
            options = NSKeyValueObservingOptionNew, context = null
        )

        NSNotificationCenter.defaultCenter().addObserver(
            this,
            selector = NSSelectorFromString(this::onPlayerItemDidPlayToEndTime.name),
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = player.currentItem
        )
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            selector = NSSelectorFromString(aSelectorName = this::onPlayerFailedToPlay.name),
            name = AVPlayerItemFailedToPlayToEndTimeNotification,
            this
        )
        addTimer(player)
        isRegisterEvent = true
    }

    @ObjCAction
    @Suppress("unused")
    fun onPlayerItemDidPlayToEndTime() {
        scope.launch {
            withContext(Dispatchers.Main) {
                listener.onPlayerItemDidPlayToEndTime(this@VideoSafeObserver)
            }
        }
    }

    @ObjCAction
    @Suppress("unused")
    fun onPlayerFailedToPlay() {
        scope.launch {
            withContext(Dispatchers.Main) {
                listener.onPlayerFailedToPlay(this@VideoSafeObserver)
            }
        }
    }

    fun removePlayerEventObserver(player: AVPlayer) {
        removeTimer(player)
        if (isRegisterEvent.not()) return
        player.removeObserver(this, forKeyPath = "rate")
        player.removeObserver(this, forKeyPath = "timeControlStatus")
        NSNotificationCenter.defaultCenter().removeObserver(this)
        isRegisterEvent = false
    }

    private var isRegisterItemEvent = false
    fun addPlayerItemEventObserver(playerItem: AVPlayerItem?) {
        if (isRegisterItemEvent) return
        if (playerItem == null) return
        playerItem.addObserver(
            this, forKeyPath = "status",
            options = NSKeyValueObservingOptionNew, context = null
        )
        playerItem.addObserver(
            this, forKeyPath = "playbackBufferEmpty",
            options = NSKeyValueObservingOptionNew, context = null
        )
        playerItem.addObserver(
            this, forKeyPath = "playbackLikelyToKeepUp",
            options = NSKeyValueObservingOptionNew, context = null
        )
        playerItem.addObserver(
            this, forKeyPath = "playbackBufferFull",
            options = NSKeyValueObservingOptionNew, context = null
        )

        isRegisterItemEvent = true
    }

    fun removeTimer(player: AVPlayer) {
        timeObserver?.let {
            player.removeTimeObserver(it)
            timeObserver = null
        }
    }

    private var timeObserver: Any? = null
    fun addTimer(player: AVPlayer) {
        if (timeObserver != null) return
        timeObserver = player.addPeriodicTimeObserverForInterval(
            CMTimeMake(value = 50, timescale = 1000),//300毫秒回调一次
            dispatch_get_main_queue()
        ) { time ->
            if(player.rate==0f){
                listener.onPlayerState(this@VideoSafeObserver,player.currentItem?.status)
            }

            if (player.status == AVPlayerItemStatusFailed) {
                listener.onPlayerFailedToPlay(this@VideoSafeObserver)
                return@addPeriodicTimeObserverForInterval
            }
            listener.onPlaying(this@VideoSafeObserver,
                CMTimeGetMilliseconds(player.currentTime()).coerceAtLeast(0.0).toLong(),
                (player.currentItem?.duration?.let {
                    CMTimeGetMilliseconds(it)
                } ?: 0.0).coerceAtLeast(0.0).toLong()
            )
        }
    }

    fun removePlayerItemEventObserver(playerItem: AVPlayerItem?) {
        if (isRegisterItemEvent.not()) return
        if (playerItem != null) {
            playerItem.removeObserver(this, forKeyPath = "status")
            playerItem.removeObserver(this, forKeyPath = "playbackBufferEmpty")
            playerItem.removeObserver(this, forKeyPath = "playbackLikelyToKeepUp")
            playerItem.removeObserver(this, forKeyPath = "playbackBufferFull")
        }
        isRegisterItemEvent = false
    }

}


@OptIn(BetaInteropApi::class)
private fun NSURL.Companion.toSafeUrl(value: FileImpl): NSURL? {
    fun formatPath(path: String): String {
        return if (path.startsWith("file://")) {
            path
        } else {
            NSString.create(string = "file://")
                .stringByAppendingString(path)
        }
    }
    return NSURL.URLWithString(
        URLString = formatPath(
            NSString.create(string = value.absolutePath)
                .stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet)
                ?: ""
        )
    )
}

private fun NSString.encodeAsURLPath(): String {
    return this.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet)
        ?: this.toString()
}

private fun AVQueuePlayer.playVideo(speed: PlayerSpeed) {
    val cur = this.currentItem ?: return
    this.setRate(
        when (speed) {
            PlayerSpeed.X0_5 -> 0.5f
            PlayerSpeed.X1 -> 1f
            PlayerSpeed.X1_5 -> 1.5f
            PlayerSpeed.X2 -> 2f
        }
    )
    if (cur.status == AVPlayerItemStatusReadyToPlay) {
        this.play()
    }
}

private fun AVPlayer.pauseVideo() {
    this.pause()
}

private val AVPlayer.isPlaying: Boolean
    get() = this.rate > 0f


actual typealias CMPPlayer = AVQueuePlayer

actual fun CMPPlayer.release() {
    this.pause()
    //https://forums.developer.apple.com/forums/thread/717629
    // 不能使用 removeAllItems()，否则会导致崩溃
    // 遍历并移除所有项目
    this.safeRemoveAllItems()
}

private fun CMPPlayer.safeRemoveAllItems() {
    while (this.items().isNotEmpty()) {
        val item = this.items().firstOrNull() as? AVPlayerItem ?: continue
        
        this.removeItem(item)
        
    }
}


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun CMPPlayer.getCurrentFirstFrame(size: Size): ImageBitmap? {
    // 确保当前播放器有播放的项目
    val currentItem = this.currentItem ?: return null
    // 获取当前播放的 URL
    val asset = currentItem.asset as? AVURLAsset ?: return null
//    val url = urlAsset.URL().absoluteString ?: return null
//    val asset =  AVAsset.assetWithURL(NSURL.URLWithString(this.currentItem?.url)!!)


    return autoreleasepool {
        val assetImgGenerate = AVAssetImageGenerator(asset)
        assetImgGenerate.appliesPreferredTrackTransform = true
        val time = CMTimeMake(value = 1, timescale = 1)
        try {
            val imageRef: CGImageRef? = assetImgGenerate.copyCGImageAtTime(time, null, null)
            imageRef?.let {
                // 转换为 Compose 的 ImageBitmap
                val ui = UIImage(cGImage = it).toImageBitmap()
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

private fun UIImage.toImageBitmap(): ImageBitmap {
    val skiaImage = this.toSkiaImage() ?: return ImageBitmap(1, 1)
    return skiaImage.toComposeImageBitmap()
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toSkiaImage(): Image? {
    val imageRef = this.CGImage ?: return null

    val width = CGImageGetWidth(imageRef).toInt()
    val height = CGImageGetHeight(imageRef).toInt()

    val bytesPerRow = CGImageGetBytesPerRow(imageRef)
    val data = CGDataProviderCopyData(CGImageGetDataProvider(imageRef))
    val bytePointer = CFDataGetBytePtr(data)
    val length = CFDataGetLength(data)

    val alphaType = when (CGImageGetAlphaInfo(imageRef)) {
        CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst,
        CGImageAlphaInfo.kCGImageAlphaPremultipliedLast -> ColorAlphaType.PREMUL

        CGImageAlphaInfo.kCGImageAlphaFirst,
        CGImageAlphaInfo.kCGImageAlphaLast -> ColorAlphaType.UNPREMUL

        CGImageAlphaInfo.kCGImageAlphaNone,
        CGImageAlphaInfo.kCGImageAlphaNoneSkipFirst,
        CGImageAlphaInfo.kCGImageAlphaNoneSkipLast -> ColorAlphaType.OPAQUE

        else -> ColorAlphaType.UNKNOWN
    }

    val byteArray = ByteArray(length.toInt()) { index ->
        bytePointer!![index].toByte()
    }

    CFRelease(data)
    CGImageRelease(imageRef)

    val skiaColorSpace = ColorSpace.sRGB
    val colorType = ColorType.RGBA_8888

    // Convert RGBA to BGRA
    for (i in byteArray.indices step 4) {
        val r = byteArray[i]
        val g = byteArray[i + 1]
        val b = byteArray[i + 2]
        val a = byteArray[i + 3]

        byteArray[i] = b
        byteArray[i + 2] = r
    }

    return Image.makeRaster(
        imageInfo = ImageInfo(
            width = width,
            height = height,
            colorType = colorType,
            alphaType = alphaType,
            colorSpace = skiaColorSpace
        ),
        bytes = byteArray,
        rowBytes = bytesPerRow.toInt(),
    )
}

@OptIn(ExperimentalForeignApi::class)
actual fun CMPPlayer.seekTo(position: Long) {
    val time = CMTimeMake(value = position, timescale = 1000)
    this.seekToTime(time)
}
