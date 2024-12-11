package org.uooc.compose.data.di.module

import coil3.PlatformContext
import com.dokar.sonner.ToasterState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.uooc.compose.core.database.dao.DownloadTaskStatus

class BaijiaLive {

    private val baijiaLiveImpl = BaijiaLiveImpl()

    companion object {
        const val liveTag = ".at.baijiayun.com/web/room/enter"
        const val playTag = ".at.baijiayun.com/web/playback/index"
    }

    suspend fun showPlayback(
        context: PlatformContext,
        url: String,
        isVideoAuto: Boolean
    ): Pair<Boolean, String> {
        val completer = CompletableDeferred<Pair<Boolean, String>>()
        withContext(Dispatchers.IO) {
            // 去掉https://头
            val urlSchema = url.replace("https://", "")
            // 用'?'进行分割
            val urlComponents = urlSchema.split("?")
            val map = mutableMapOf<String, Any>();
            // 百家云id
            val partnerId = urlComponents.first().replace(playTag, "")

            println("bjid: $partnerId")
            // 用'&'进行分割
            val lastUrlComponents = urlComponents.last().split("&")
            for (keyValuePair in lastUrlComponents) {
                // 用'='进行分割
                val pairComponents = keyValuePair.split("=")
                val key = pairComponents.first()
                val value = pairComponents.last()
                map[key] = value
            }
            // 弹出百家云播放页面
            try {
                val classId = "${map["classid"]}"
                val token = "${map["token"]}"
                val userName = "${map["user_name"]}"
                val userNumber = "${map["user_number"]}"
                completer.complete(
                    baijiaLiveImpl.enterPBRoom(
                        context,
                        classId.toLong(),
                        token,
                        userName,
                        userNumber,
                        partnerId
                    )
                )

            } catch (e: Exception) {
                completer.complete(false to "Failed to show Room ${e.message}")
            }
        }
        return completer.await()
    }

    suspend fun showLocalPlayback(
        context: PlatformContext,
        item: BjyCommonDownloadItem,
        isVideoAuto: Boolean
    ): Pair<Boolean, String> {
        val completer = CompletableDeferred<Pair<Boolean, String>>()
        withContext(Dispatchers.IO) {
            // 弹出百家云播放页面
            try {
                completer.complete(
                    baijiaLiveImpl.enterLocalPBRoom(
                        context,
                        item
                    )
                )
            } catch (e: Exception) {
                completer.complete(false to "Failed to show Room ${e.message}")
            }
        }
        return completer.await()
    }

    suspend fun showLiveRoom(
        context: PlatformContext,
        url: String,
        isVideoAuto: Boolean
    ): Pair<Boolean, String> {
        val completer = CompletableDeferred<Pair<Boolean, String>>()
        withContext(Dispatchers.IO) {
            // 去掉https://头
            val urlSchema = url.replace("https://", "")
            // 用'?'进行分割
            val urlComponents = urlSchema.split("?")
            val map = mutableMapOf<String, Any>();
            // 百家云id
            val partnerId = urlComponents.first().replace(liveTag, "")

            println("bjid: $partnerId")
            // 用'&'进行分割
            val lastUrlComponents = urlComponents.last().split("&")
            for (keyValuePair in lastUrlComponents) {
                // 用'='进行分割
                val pairComponents = keyValuePair.split("=")
                val key = pairComponents.first()
                val value = pairComponents.last()
                map[key] = value
            }
            // 弹出百家云播放页面
            try {
                val roomId = "${map["room_id"] ?: 0}".toLong()
                val sign = "${map["sign"]}"
                val userName = "${map["user_name"]}"
                val userAvatar = "${map["user_avatar"]}"
                val userNumber = "${map["user_number"]}"
                val userRole = "${map["user_role"]}".toInt()
                println("roomId: $roomId, sign: $sign, userName: $userName, userAvatar: $userAvatar, userNumber: $userNumber, userRole: $userRole, partnerId: $partnerId")
                completer.complete(
                    baijiaLiveImpl.enterBigClass(
                        context,
                        roomId,
                        sign,
                        userName,
                        userAvatar,
                        userNumber,
                        userRole,
                        partnerId
                    )
                )
            } catch (e: Exception) {
                completer.complete(false to "Failed to show Room ${e.message}")
            }
        }
        return completer.await()
    }

    suspend fun setPrivacyNeedGrant(context: PlatformContext) {
        baijiaLiveImpl.setPrivacyNeedGrant(context)
    }


    suspend fun initDownloadManager(context: PlatformContext,identifier: String) {
        baijiaLiveImpl.downloadManager.initDownloadManager(context,identifier)
    }

    fun downloadManager(): DownloadManager {
        return baijiaLiveImpl.downloadManager
    }

}

expect class BaijiaLiveImpl() {
    suspend fun enterPBRoom(
        context: PlatformContext,
        roomId: Long,
        roomToken: String,
        userName: String,
        userNumber: String,
        partnerId: String
    ): Pair<Boolean, String>

    suspend fun enterBigClass(
        context: PlatformContext,
        roomId: Long,
        sign: String,
        userName: String,
        userAvatar: String,
        userNumber: String,
        userRole: Int,
        partnerId: String
    ): Pair<Boolean, String>

    suspend fun enterLocalPBRoom(
        context: PlatformContext,
        videoModel: BjyCommonDownloadItem,
    ): Pair<Boolean, String>

    fun setPrivacyNeedGrant(context: PlatformContext)

    val downloadManager: DownloadManager
}

expect class DownloadManager {
    fun initDownloadManager(context: PlatformContext,identifier: String)

    suspend fun addDownloadTask(
        id: String, classID: String, token: String, resourceId: String,
        resourceName: String,
        fileName: String,
        category: Int,
        examType: Int,
        level: Int
    ): BjyCommonDownloadItem?

    suspend fun getUnCompletedTask(): List<BjyCommonDownloadItem>
    suspend fun getDownloadingTask(): List<BjyCommonDownloadItem>
    suspend fun getAllTask(): List<BjyCommonDownloadItem>

    suspend fun shutDown()
    suspend fun resumeTask(task: BjyCommonDownloadItem, toaster: ToasterState)
    suspend fun pauseTask(task: BjyCommonDownloadItem)

//    fun deleteTask(classID: String, token: String)
}


@Serializable
data class BjUserExtraInfo(
    @SerialName("fakeFid")
    val fakeFid: String = "",
    @SerialName("userName")
    val userName: String = "",
    @SerialName("userNumber")
    val userNumber: String = "",
    @SerialName("resourceId")
    val resourceId: String = "",
    @SerialName("resourceName")
    val resourceName: String = "",
    @SerialName("fileName")
    val fileName: String = "",
    @SerialName("category")
    val category: Int = 0,
    @SerialName("level")
    val level: Int = 0,
    @SerialName("examType")
    val examType: Int = 0
)


/**
 *
 * ios 中BJVDownloadItem的属性
 *
 *     public expect final var accessKey: kotlin.String? /* compiled code */
 *
 *     public expect final val classID: kotlin.String? /* compiled code */
 *
 *     public expect final val clipedVersion: platform.darwin.NSInteger /* = kotlin.Long */ /* compiled code */
 *
 *     public expect final val coverImageFile: what.the.fuck.with.baijiayun.playcore.BJLDownloadFile? /* compiled code */
 *
 *     public expect final val currentDefinitionInfo: what.the.fuck.with.baijiayun.playcore.BJVDefinitionInfo /* compiled code */
 *
 *     public expect final val isEncrypted: kotlin.Boolean /* compiled code */
 *
 *     public expect final val isPlayback: kotlin.Boolean /* compiled code */
 *
 *     public expect final val playInfo: what.the.fuck.with.baijiayun.playcore.BJVPlayInfo? /* compiled code */
 *
 *     public expect final val sessionID: kotlin.String? /* compiled code */
 *
 *     public expect final val signalFile: what.the.fuck.with.baijiayun.playcore.BJLDownloadFile? /* compiled code */
 *
 *     public expect final val subtitleFiles: kotlin.collections.List<*>? /* compiled code */
 *
 *     public expect final var token: kotlin.String? /* compiled code */
 *
 *     public expect final var userInfo: kotlin.collections.Map<kotlin.Any?, *>? /* compiled code */
 *
 *     public expect final var userInfoString: kotlin.String? /* compiled code */
 *
 *     public expect final val videoFile: what.the.fuck.with.baijiayun.playcore.BJLDownloadFile? /* compiled code */
 *
 *     public expect final val videoID: kotlin.String? /* compiled code */
 *
 *     public expect final val watermarkImageFile: what.the.fuck.with.baijiayun.playcore.BJLDownloadFile? /* compiled code */
 *
 * android实现
 *   public long videoId;
 *     public long sessionId;
 *     public long roomId;
 *     public int version = -1;
 *     public String url;
 *     public VideoDefinition definition;
 *     public long videoDuration;
 *     public FileType fileType;
 *     public String targetName;
 *     public String videoName;
 *     public TaskStatus status;
 *     public String targetFolder;
 *     public long totalLength;
 *     public long downloadLength;
 *     public long speed;
 *     public Serializable data;
 *     public boolean isEncrypt;
 *     public String videoToken;
 *     public String extraInfo;
 *     public String coverUrl;
 *     public PartnerConfig partnerConfig;
 *     public PlayItem playItem;
 *     public transient LinkedList<String> availableCDN;
 *     public int recordType;
 *     public int smallCourseRecordType;
 *     public int live1v1BlackboardPages;
 *     public String whiteboardUrl;
 *     public long partnerId;
 *     public String guid;
 *     public DownloadModel nextModel;
 *     @SerializedName("template")
 *     public LPConstants.TemplateType templateName;
 *     public List<SubtitleItem> subtitleItems;
 *     @SerializedName("key_frame_desc_list")
 *     public List<LPKeyFrameModel> keyFrameModelList;
 *     public int isVideoMain;
 *     public long invalidTs;
 *     @SerializedName("live_hourse_lamp")
 *     public LPHorseLamp horseLamp;
 */
// 下面是结合上面两个平台的属性，定义一个通用的下载item
expect interface BjyCommonDownloadItem {
    fun isNotComplete(): Boolean
    fun delete(bjDownloadManager: DownloadManager)

    val videoId: String
    val sessionId: String
    val extraInfo: BjUserExtraInfo
    val status: DownloadTaskStatus
    val isEncrypted: Boolean
    val coverUrl: String
    val totalLength: Long
    val downloadLength: Long
    val speed: Long
}