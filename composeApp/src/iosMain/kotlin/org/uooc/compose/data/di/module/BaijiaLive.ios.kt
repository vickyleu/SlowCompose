package org.uooc.compose.data.di.module


import androidx.compose.runtime.mutableStateOf
import coil3.PlatformContext
import com.dokar.sonner.ToasterState
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.uooc.compose.core.database.dao.DownloadTaskStatus
import org.uooc.compose.core.uoocDispatchers
import platform.Foundation.NSError
import platform.Foundation.NSURLSessionConfiguration
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import what.the.fuck.with.baijiayun.live.cinterop.uibase.BJLErrorProtocol
import what.the.fuck.with.baijiayun.live.cinterop.uibase.BJLRoom
import what.the.fuck.with.baijiayun.live.cinterop.uibase.BJLRoomID
import what.the.fuck.with.baijiayun.live.cinterop.uibase.BJLRoomVCDelegateProtocol
import what.the.fuck.with.baijiayun.live.cinterop.uibase.BJLRoomVCTypeBigClass
import what.the.fuck.with.baijiayun.live.cinterop.uibase.BJLRoomViewController
import what.the.fuck.with.baijiayun.live.cinterop.uibase.BJLUser
import what.the.fuck.with.baijiayun.live.cinterop.uibase.BJLUserRole_assistant
import what.the.fuck.with.baijiayun.live.cinterop.uibase.BJLUserRole_guest
import what.the.fuck.with.baijiayun.live.cinterop.uibase.BJLUserRole_student
import what.the.fuck.with.baijiayun.live.cinterop.uibase.BJLUserRole_teacher
import what.the.fuck.with.baijiayun.live.cinterop.uibase.bjl_keyWindow
import what.the.fuck.with.baijiayun.live.cinterop.uibase.bjl_presentFullScreenViewController
import what.the.fuck.with.baijiayun.live.cinterop.uibase.bjl_visibleViewController
import what.the.fuck.with.baijiayun.playback.cinterop.core.BJLDownloadItemState_completed
import what.the.fuck.with.baijiayun.playback.cinterop.core.BJLDownloadItemState_initial
import what.the.fuck.with.baijiayun.playback.cinterop.core.BJLDownloadItemState_invalid
import what.the.fuck.with.baijiayun.playback.cinterop.core.BJLDownloadItemState_paused
import what.the.fuck.with.baijiayun.playback.cinterop.core.BJLDownloadItemState_running
import what.the.fuck.with.baijiayun.playback.cinterop.core.BJVRequestTokenDelegateProtocol
import what.the.fuck.with.baijiayun.playback.cinterop.core.BJVTokenManager
import what.the.fuck.with.baijiayun.playback.cinterop.ui.BJLDownloadManager
import what.the.fuck.with.baijiayun.playback.cinterop.ui.BJLDownloadManagerClassDelegateProtocol
import what.the.fuck.with.baijiayun.playback.cinterop.ui.BJLDownloadManagerDelegateProtocol
import what.the.fuck.with.baijiayun.playback.cinterop.ui.BJPPlaybackOptions
import what.the.fuck.with.baijiayun.playback.cinterop.ui.BJPRoomViewController
import what.the.fuck.with.baijiayun.playback.cinterop.ui.BJVAppConfig
import what.the.fuck.with.baijiayun.playback.cinterop.ui.BJVDownloadItem
import what.the.fuck.with.baijiayun.playback.cinterop.ui.addDownloadItemWithClassID
import what.the.fuck.with.baijiayun.playback.cinterop.ui.validateItemWithClassID


actual class BaijiaLiveImpl actual constructor() : NSObject(), BJLRoomVCDelegateProtocol {
    private val meetingJoinFlow = MutableStateFlow<Pair<Boolean, String>?>(null)
    private val scope = MainScope()

    actual suspend fun enterBigClass(
        context: PlatformContext,
        roomId: Long,
        sign: String,
        userName: String,
        userAvatar: String,
        userNumber: String,
        userRole: Int,
        partnerId: String
    ): Pair<Boolean, String> {
        liveJob?.cancel()

        pbJob?.cancel()
        val completer = CompletableDeferred<Pair<Boolean, String>>()
        withContext(uoocDispatchers.io) {
            BJLRoom.setPrivateDomainPrefix(partnerId)
            val room = BJLRoomID()
            room.setRoomID(roomId.toString())
            room.setApiSign(sign)
            val role = when (userRole) {
                0 -> BJLUserRole_student
                1 -> BJLUserRole_teacher
                2 -> BJLUserRole_assistant
                3 -> BJLUserRole_guest
                else -> BJLUserRole_student
            }

            room.setUser(
                BJLUser.userWithNumber(
                    userNumber,
                    userName,
                    0,
                    userAvatar,
                    role
                )
            )
            withContext(uoocDispatchers.main) {
                val controller = BJLRoomViewController.instanceWithRoomType(
                    type = BJLRoomVCTypeBigClass,
                    roomID = room
                ) as BJLRoomViewController
                liveJob = scope.launch {
                    withContext(Dispatchers.IO) {
                        meetingJoinFlow.filterNotNull()
                            .distinctUntilChanged()
                            .collectLatest {
                                completer.complete(it)
                            }
                    }
                }
                UIWindow.bjl_keyWindow?.bjl_visibleViewController?.bjl_presentFullScreenViewController(
                    controller, true
                ) {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            delay(1000)
                            if (completer.isCompleted.not()) {
                                completer.complete(false to "直播间进入失败")
                            }
                        }
                    }
                } ?: run {
                    completer.complete(false to "直播间进入失败")
                }
            }
        }
        return completer.await()
    }

    actual fun setPrivacyNeedGrant(context: PlatformContext) {
//        downloadManager.initDownloadManager(context)
    }

    override fun roomViewControllerEnterRoomSuccess(roomViewController: BJLRoomViewController) {
        meetingJoinFlow.tryEmit(true to "直播间进入成功")
    }

    override fun roomViewControllerNeedCharge(
        roomViewController: BJLRoomViewController,
        closeCallback: (() -> Unit)?
    ): UIViewController? {
        closeCallback?.invoke()
        return null
    }

    override fun roomViewController(
        roomViewController: BJLRoomViewController,
        didExitWithError: NSError?
    ) {
        meetingJoinFlow.tryEmit(false to "${didExitWithError?.localizedDescription ?: "未知错误"}")
    }

    override fun roomViewController(
        roomViewController: BJLRoomViewController,
        enterRoomFailureWithError: NSError
    ) {
        val error = enterRoomFailureWithError as BJLErrorProtocol
        meetingJoinFlow.tryEmit(false to "${error.bjl_sourceError() ?: "未知错误"}")
    }

    private var pbJob: Job? = null
    private var liveJob: Job? = null

    actual suspend fun enterPBRoom(
        context: PlatformContext,
        roomId: Long,
        roomToken: String,
        userName: String,
        userNumber: String,
        partnerId: String
    ): Pair<Boolean, String> {
        liveJob?.cancel()
        pbJob?.cancel()
        val completer = CompletableDeferred<Pair<Boolean, String>>()
        withContext(uoocDispatchers.io) {
            BJVAppConfig.sharedInstance().setPrivateDomainPrefix(partnerId)
            val options = BJPPlaybackOptions()
            options.autoplay = true
            options.userName = userName
            options.userNumber = userNumber
            options.encryptEnabled = false
            options.pictureInPictureEnabled = true
            options.backgroundAudioEnabled = true
            options.playTimeRecordEnabled = true
            options.sliderDragEnabled = true
            withContext(uoocDispatchers.main) {
                val playbackController = BJPRoomViewController.onlinePlaybackRoomWithClassID(
                    roomId.toString(),
                    "-1",
                    roomToken,
                    "-1",
                    options
                ) as BJPRoomViewController
                pbJob = scope.launch {
                    withContext(Dispatchers.IO) {
                        meetingJoinFlow.filterNotNull()
                            .distinctUntilChanged()
                            .collectLatest {
                                completer.complete(it)
                            }
                    }
                }
                UIWindow.bjl_keyWindow?.bjl_visibleViewController?.bjl_presentFullScreenViewController(
                    playbackController, true
                ) {
                    completer.complete(true to "直播间进入成功")
                } ?: run {
                    completer.complete(false to "回放进入失败")
                }
            }
        }
        return completer.await()
    }

    actual val downloadManager = DownloadManager()
    actual suspend fun enterLocalPBRoom(
        context: PlatformContext,
        videoModel: BjyCommonDownloadItem
    ): Pair<Boolean, String> {
        val completer = CompletableDeferred<Pair<Boolean, String>>()
        withContext(Dispatchers.IO) {
            val options = BJPPlaybackOptions()
            options.autoplay = true
            options.userName = videoModel.extraInfo.userName
            options.userNumber = videoModel.extraInfo.userNumber
            options.encryptEnabled = false
            options.backgroundAudioEnabled = true
            options.pictureInPictureEnabled = true
            options.playTimeRecordEnabled = true
            options.sliderDragEnabled = true
            withContext(uoocDispatchers.main) {
                val playbackController = BJPRoomViewController.localPlaybackRoomWithDownloadItem(
                    videoModel.originTask,
                    options
                ) as BJPRoomViewController
                UIWindow.bjl_keyWindow?.bjl_visibleViewController?.bjl_presentFullScreenViewController(
                    playbackController, true
                ) {
                    completer.complete(true to "")
                } ?: run {
                    completer.complete(false to "回放进入失败")
                }
            }
        }
        return completer.await()
    }

}

typealias BJVDownloadManager = BJLDownloadManager

actual class DownloadManager : NSObject(), BJLDownloadManagerClassDelegateProtocol,
    BJVRequestTokenDelegateProtocol, BJLDownloadManagerDelegateProtocol {
    private lateinit var iosDownloadManager: BJVDownloadManager
    actual fun initDownloadManager(context: PlatformContext, identifier: String) {
        if (::iosDownloadManager.isInitialized) {
            if (iosDownloadManager.identifier == identifier) return
            iosDownloadManager = BJVDownloadManager.downloadManagerWithIdentifier(identifier, true)
            iosDownloadManager.delegate = this
            return
        }
        BJVTokenManager.tokenDelegate = this
        BJVDownloadManager.setClassDelegate(this)
        BJLDownloadManager.setClassDelegate(this)
        iosDownloadManager = BJVDownloadManager.downloadManagerWithIdentifier(identifier, true)
        iosDownloadManager.delegate = this
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private var tokenTemp = mutableStateOf("")
    actual suspend fun addDownloadTask(
        id: String, classID: String, token: String, resourceId: String,
        resourceName: String,
        fileName: String,
        category: Int,
        examType: Int,
        level: Int
    ): BjyCommonDownloadItem? {
        val completer =
            CompletableDeferred<BJVDownloadItem?>()
        tokenTemp.value = token
        val valid = iosDownloadManager.validateItemWithClassID(classID, "-1")
        if (valid) {
            val item = iosDownloadManager.addDownloadItemWithClassID(
                classID, "-1", true,
                preferredDefinitionList = listOf(
                    "1080p",
                    "720p",
                    "superHD",
                    "high",
                    "low",
                    "audio"
                ),
                setting = {
                    it?.apply {
                        this.setToken(token)
                        val extraInfo = BjUserExtraInfo(
                            fakeFid = id,
                            resourceId = resourceId,
                            resourceName = resourceName,
                            fileName = fileName,
                            category = category,
                            level = level,
                            examType = examType,
                        )
                        this.setUserInfoString(json.encodeToString(extraInfo))
                    }
                })
            item?.apply {
                if (this.state == BJLDownloadItemState_paused) {
                    this.resume()
                } else if (this.state == BJLDownloadItemState_initial) {
                    this.resume()
                }
            }
            completer.complete(item)
        } else {
            completer.complete(null)
        }
        val item = completer.await()
        return item?.toBjyCommonDownloadItem()
    }

    override fun requestTokenWithClassID(
        classID: String,
        sessionID: String?,
        completion: (String?, NSError?) -> Unit
    ) {
        completion(tokenTemp.value, null)
    }

    override fun requestTokenWithVideoID(videoID: String, completion: (String?, NSError?) -> Unit) {
        completion(tokenTemp.value, null)
    }


    private fun BJVDownloadItem.toBjyCommonDownloadItem(): BjyCommonDownloadItem {
        return BjyCommonDownloadItem(json, this)
    }

    override fun downloadManager(
        downloadManager: BJLDownloadManager, URLSessionConfiguration: NSURLSessionConfiguration
    ) {
        // 设置是否允许使用蜂窝网络下载
        URLSessionConfiguration.allowsCellularAccess = true

    }

    actual suspend fun getUnCompletedTask(): List<BjyCommonDownloadItem> {
        return getAllTask().filter {
            it.originTask.state != BJLDownloadItemState_completed
        }
    }

    actual suspend fun getDownloadingTask(): List<BjyCommonDownloadItem> {
        return getAllTask().filter {
            it.originTask.state in listOf(BJLDownloadItemState_running)
        }
    }

    // 千万不要打印百家云的对象, println直接打印会导致release模式下崩溃
    actual suspend fun getAllTask(): List<BjyCommonDownloadItem> {
        if (::iosDownloadManager.isInitialized.not()) return emptyList()
        return (iosDownloadManager.downloadItems as? List<BJVDownloadItem>)?.map {
            it.toBjyCommonDownloadItem()
        } ?: emptyList()
    }

    actual suspend fun shutDown() {
        if (::iosDownloadManager.isInitialized.not()) return
        (iosDownloadManager.downloadItems as? List<BJVDownloadItem>)?.forEach {
            it.pause()
        }
    }

    actual suspend fun resumeTask(
        task: BjyCommonDownloadItem,
        toaster: ToasterState
    ) {
        task.originTask.resume()
    }

    actual suspend fun pauseTask(task: BjyCommonDownloadItem) {
        task.originTask.pause()
    }

    fun deleteTask(originTask: BJVDownloadItem) {
        iosDownloadManager.removeDownloadItemWithIdentifier(originTask.itemIdentifier)
    }
}

fun BjyCommonDownloadItem(json: Json, originTask: BJVDownloadItem): BjyCommonDownloadItem {
    val convertState: DownloadTaskStatus = when (originTask.state) {
        BJLDownloadItemState_initial -> DownloadTaskStatus.PENDING
        BJLDownloadItemState_running -> DownloadTaskStatus.DOWNLOADING
        BJLDownloadItemState_paused -> DownloadTaskStatus.PAUSED
        BJLDownloadItemState_invalid -> DownloadTaskStatus.FAILED
        BJLDownloadItemState_completed -> DownloadTaskStatus.COMPLETED
        else -> DownloadTaskStatus.PENDING
    }
    val info = json.decodeFromString<BjUserExtraInfo>(originTask.userInfoString!!)
    return BjyCommonDownloadItemImpl(
        originTask = originTask,
        videoId = originTask.videoID.toString(),
        sessionId = originTask.sessionID.toString(),
        isEncrypted = originTask.isEncrypted,
        coverUrl = originTask.coverImageFile?.filePath ?: "",
        totalLength = originTask.totalSize,
        downloadLength = originTask.progress.useContents {
            (this.fractionCompleted * this.totalUnitCount).toLong()
        },
        speed = originTask.bytesPerSecond,
        extraInfo = info,
        status = convertState
    )
}

class BjyCommonDownloadItemImpl(
    override val originTask: BJVDownloadItem,
    override val videoId: String,
    override val sessionId: String,
    override val isEncrypted: Boolean,
    override val coverUrl: String,
    override val totalLength: Long,
    override val downloadLength: Long,
    override val speed: Long,
    override val extraInfo: BjUserExtraInfo,
    override val status: DownloadTaskStatus
) : BjyCommonDownloadItem {
    override fun isNotComplete(): Boolean {
        return true
    }

    override fun delete(manager: DownloadManager) {
        originTask.pause()
        manager.deleteTask(originTask)
    }

}

actual interface BjyCommonDownloadItem {
    val originTask: BJVDownloadItem
    actual val videoId: String
    actual val sessionId: String
    actual val isEncrypted: Boolean
    actual val coverUrl: String
    actual val totalLength: Long
    actual val downloadLength: Long
    actual val speed: Long
    actual fun isNotComplete(): Boolean
    actual val extraInfo: BjUserExtraInfo
    actual val status: DownloadTaskStatus
    actual fun delete(bjDownloadManager: DownloadManager)
}
