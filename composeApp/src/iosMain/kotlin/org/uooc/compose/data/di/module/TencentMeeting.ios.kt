package org.uooc.compose.data.di.module


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import coil3.PlatformContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.uooc.compose.core.uoocDispatchers
import platform.UIKit.UIApplication
import platform.darwin.NSInteger
import platform.darwin.NSObject
import what.the.fuck.with.tencent.TMAuthenticationProtocolProtocol
import what.the.fuck.with.tencent.TMInMeetingProtocolProtocol
import what.the.fuck.with.tencent.TMInitParam
import what.the.fuck.with.tencent.TMJoinParam
import what.the.fuck.with.tencent.TMLeaveType
import what.the.fuck.with.tencent.TMLogoutType
import what.the.fuck.with.tencent.TMPreMeetingProtocolProtocol
import what.the.fuck.with.tencent.TMSDKActionType
import what.the.fuck.with.tencent.TMSDKInviteType
import what.the.fuck.with.tencent.TMSDKProtocolProtocol
import what.the.fuck.with.tencent.TMSDKResult
import what.the.fuck.with.tencent.TMSDKWemeetActionType
import what.the.fuck.with.tencent.TencentMeetingSDK

actual class TencentMeeting actual constructor() : KoinComponent {
    actual val isPrivacyNeedGrant: MutableState<Boolean> = mutableStateOf(false)
    private val authenticationFlow = MutableStateFlow<Pair<TencentMeetingErrorCode, String?>?>(null)
    private val meetingJoinFlow =
        MutableStateFlow<Triple<TencentMeetingErrorCode, String, String>?>(null)
    private val meetingLeaveFlow = MutableStateFlow<Triple<Boolean, String, Any?>?>(null)
    private val scope by lazy { get<CoroutineScope>() }

    private val inMeetingCallback = object : NSObject(), TMInMeetingProtocolProtocol {
        override fun onInviteMeeting(invite_info: String) {

        }

        override fun onLeaveMeeting(
            type: TMLeaveType,
            code: TMSDKResult,
            msg: String,
            meetingCode: String
        ) {
            meetingLeaveFlow.tryEmit(Triple(code.toInt() == 0, msg, meetingCode))
        }

        override fun onShowMeetingInfo(meeting_info: String) {
        }

        override fun onSwitchPiPResult(code: TMSDKResult, msg: String) {
        }
    }
    private val authenticationCallback = object : NSObject(), TMAuthenticationProtocolProtocol {
        override fun onJumpUrlWithLoginStatus(code: TMSDKResult, msg: String) {

        }

        override fun onLogin(code: TMSDKResult, msg: String) {
            println("登录回调 $code $msg")
            authenticationFlow.tryEmit(TencentMeetingErrorCode.ofRaw(code.toInt()) to msg)
        }

        override fun onLogout(type: TMLogoutType, code: TMSDKResult, msg: String) {

        }

    }
    private val preMeetingCallback = object : NSObject(), TMPreMeetingProtocolProtocol {
        override fun onActionResult(actionType: TMSDKActionType, code: TMSDKResult, msg: String) {

        }

        override fun onJoinMeeting(code: TMSDKResult, msg: String, meetingCode: String) {
            meetingJoinFlow.tryEmit(
                Triple(
                    TencentMeetingErrorCode.ofRaw(code.toInt()),
                    msg,
                    meetingCode
                )
            )
        }

        override fun onShowAddressBook(userType: TMSDKInviteType, users: String) {
        }

        override fun onShowScreenCastViewResult(code: TMSDKResult, msg: String) {
        }
    }

    actual companion object {
        actual fun preloadSdk(context: PlatformContext) {
        }
    }

    actual fun isInitialed(): Boolean {
        return TencentMeetingSDK.instance().isInitialized()
    }

    actual fun isLoggedIn(): Boolean {
        return TencentMeetingSDK.instance().getAccountService().isLoggedIn()
    }

    actual suspend fun setPrivacyNeedGrant(context: PlatformContext) {
        isPrivacyNeedGrant.value = true
    }

    actual suspend fun logout() {
    }

    actual suspend fun refreshToken(newSdkToken: String): TencentMeetingErrorCode {
        return TencentMeetingErrorCode.ofRaw(
            TencentMeetingSDK.instance().refreshSDKToken(newSdkToken).toInt()
        )
    }

    actual suspend fun initMeeting(
        preferLanguage: String,
        serverAddress: String,
        serverDomain: String
    ): Pair<TencentMeetingErrorCode, String> {
        val result = CompletableDeferred<Pair<TencentMeetingErrorCode, String>>()
        withContext(uoocDispatchers.io){
            val param = TMInitParam()
            param.preferLanguage = preferLanguage
            param.serverAddress = serverAddress
            param.orgDomain = serverDomain
            val callback = object : NSObject(), TMSDKProtocolProtocol {
                override fun onActiveUploadLogsResult(code: TMSDKResult, msg: String) {
                    println("${this::class.simpleName} ===>> 初始化? onActiveUploadLogsResult $code $msg")
                }

                override fun onAddUsersResult(
                    userType: TMSDKInviteType,
                    code: TMSDKResult,
                    msg: String
                ) {
                    println("${this::class.simpleName} ===>> 初始化? onAddUsersResult $code $msg")
                }

                override fun onHandleSchemaResult(code: TMSDKResult, msg: String) {
                    println("${this::class.simpleName} ===>> 初始化? onHandleSchemaResult $code $msg")
                }

                override fun onHandleWemeetAction(actionType: TMSDKWemeetActionType, param: String) {
                    println("${this::class.simpleName} ===>> 初始化? onHandleWemeetAction $actionType $param")
                }

                override fun onParseMeetingInfoUrlCode(code: NSInteger, param: String) {
                    println("${this::class.simpleName} ===>> 初始化? onParseMeetingInfoUrlCode $code $param")
                }

                override fun onResetSDKState(code: TMSDKResult, msg: String) {
                    println("${this::class.simpleName} ===>> 初始化? onResetSDKState $code $msg")
                }

                override fun onSDKError(code: TMSDKResult, msg: String) {
                    println("${this::class.simpleName} ===>> 初始化? onSDKError  $code $msg")
                    result.complete(TencentMeetingErrorCode.ofRaw(code.toInt()) to msg)
                }

                override fun onSDKInitializeResult(code: TMSDKResult, msg: String) {
                    println("${this::class.simpleName} ===>> 初始化? onSDKInitializeResult $code $msg")
                    val error = TencentMeetingErrorCode.ofRaw(code.toInt())
                    when (error) {
                        TencentMeetingErrorCode.success -> {
                            println("${this::class.simpleName} ===>> 初始化? onSDKInitializeResult success  authenticationCallback>>>")
                            TencentMeetingSDK.instance().getAccountService()
                                .setDelegate(authenticationCallback)
                            TencentMeetingSDK.instance().getPreMeetingService()
                                .setDelegate(preMeetingCallback)
                            TencentMeetingSDK.instance().getInMeetingService()
                                .setDelegate(inMeetingCallback)
                        }

                        else -> Unit
                    }
                    result.complete(error to msg)
                }

                override fun onSetProxyInfoResult(code: TMSDKResult, msg: String) {
                    println("${this::class.simpleName} ===>> 初始化? onSetProxyInfoResult $code $msg")
                }

                override fun onShowLogsResult(code: TMSDKResult, msg: String) {
                    println("${this::class.simpleName} ===>> 初始化? onShowLogsResult $code $msg")
                }


            }
            withContext(uoocDispatchers.main){
                param.setKeyWindow(UIApplication.sharedApplication.keyWindow)
                TencentMeetingSDK.instance().initialize(param,callback)
                println("${this::class.simpleName} ===>> 初始化? initialize !!!")
            }
        }
        println("${this::class.simpleName} ===>> 初始化?")
        return result.await()
    }

    actual suspend fun login(): Pair<TencentMeetingErrorCode, String> {
        val result = CompletableDeferred<Pair<TencentMeetingErrorCode, String>>()
        scope.launch {
            withContext(uoocDispatchers.io) {
                authenticationFlow
                    .filterNotNull()
                    .distinctUntilChanged()
                    .collect {
                        result.complete(it.first to (it.second ?: it.first.name))
                    }

            }
        }

        return result.await()
    }

    actual suspend fun joinMeeting(
        meetingCode: String,
        cameraOn: Boolean,
        inviteUrl: String,
        userDisplayName: String,
        password: String,
        micOn: Boolean,
        faceBeautyOn: Boolean,
        speakerOn: Boolean
    ): Pair<TencentMeetingErrorCode, String> {

        val result = CompletableDeferred<Pair<TencentMeetingErrorCode, String>>()
        scope.launch {
            withContext(uoocDispatchers.io) {
                meetingJoinFlow
                    .filterNotNull()
                    .distinctUntilChanged()
                    .filter {(code,meeting_Code,error)->
                        println("joinMeeting $code $meeting_Code $error")
                        meeting_Code == meetingCode
                    }
                    .map {(code,meeting_Code,error)->
                        code to error
                    }
                    .distinctUntilChanged()
                    .collect {
                        result.complete(it)
                    }
            }
        }
        val param = TMJoinParam()
        param.meetingCode = meetingCode
        param.cameraOn = cameraOn
        param.inviteUrl = inviteUrl
        param.userDisplayName = userDisplayName
        param.password = password
        param.micOn = micOn
        param.faceBeautyOn = faceBeautyOn
        param.speakerOn = speakerOn
        scope.launch {
            withContext(uoocDispatchers.main){
                TencentMeetingSDK.instance().getPreMeetingService().joinMeeting(param)
            }
        }
        return result.await()
    }

    actual fun getUrlWithLoginStatus(videoUrl: String): String? {
        try {
            if(videoUrl.isNotEmpty() && videoUrl.startsWith("http")){
                return TencentMeetingSDK.instance().getAccountService().getUrlWithLoginStatus(videoUrl)
            }
        }catch (e:Throwable){
            e.printStackTrace()
        }
        return null
    }


}
