package org.uooc.compose.data.di.module

import androidx.compose.runtime.MutableState
import coil3.PlatformContext
import org.koin.core.component.KoinComponent


expect class TencentMeeting() : KoinComponent {
    val isPrivacyNeedGrant: MutableState<Boolean>

    companion object{
         fun preloadSdk(context: PlatformContext)
    }

    fun isInitialed(): Boolean
    fun isLoggedIn(): Boolean


     suspend fun initMeeting(
        preferLanguage: String = "",
        serverAddress: String = "",
        serverDomain: String = ""
    ): Pair<TencentMeetingErrorCode, String>

    suspend fun logout()

    suspend fun refreshToken(newSdkToken: String): TencentMeetingErrorCode

    suspend fun login(): Pair<TencentMeetingErrorCode, String>

     suspend fun joinMeeting(
        meetingCode: String,
        cameraOn: Boolean,
        inviteUrl: String,
        userDisplayName: String,
        password: String,
        micOn: Boolean,
        faceBeautyOn: Boolean,
        speakerOn: Boolean
    ): Pair<TencentMeetingErrorCode, String>

    suspend fun setPrivacyNeedGrant(context: PlatformContext)

    fun getUrlWithLoginStatus(videoUrl: String): String?
}

@Suppress("EnumEntryName")
enum class TencentMeetingErrorCode(val raw: Int) {
    success(0),
    serverConfigFail(-1001),
    invalidAuthCode(-1002),
    logoutInMeeting(-1003),
    unknown(-1005),
    userNotAuthorized(-1006),
    userInMeeting(-1007),
    invalidParam(-1008),
    invalidMeetingCode(-1009),
    invalidNickname(-1010),
    duplicateInitCall(-1011),
    accountAlreadyLogin(-1012),
    sdkNotInitialized(-1013),
    syncCallTimeout(-1014),
    notInMeeting(-1015),
    cancelJoin(-1016),
    isLogining(-1017),
    loginNetError(-1018),
    tokenVerifyFailed(-1019),
    childProcessCrash(-1020),
    multiAccountLoginConflict(-1021),
    joinMeetingServiceFailed(-1022),
    invalidJsonString(-1024),
    proxySetFailed(-1025),
    unknownError(-1);

    companion object {
        fun ofRaw(raw: Int): TencentMeetingErrorCode {
            return entries.firstOrNull { it.raw == raw } ?: unknownError
        }
    }
}