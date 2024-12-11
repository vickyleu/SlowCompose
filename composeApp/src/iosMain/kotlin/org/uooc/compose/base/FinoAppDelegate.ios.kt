@file:Suppress("unused","unchecked_cast")
package org.uooc.compose.base

import what.the.fuck.finclip.FATApiCryptType
import what.the.fuck.finclip.FATAppletQrCodeRequest
import what.the.fuck.finclip.FATAppletRequest
import what.the.fuck.finclip.FATClient
import what.the.fuck.finclip.FATConfig
import what.the.fuck.finclip.FATExtensionCodeFailure
import what.the.fuck.finclip.FATExtensionCodeSuccess
import what.the.fuck.finclip.FATStoreConfig
import coil3.PlatformContext
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.uooc.compose.core.uoocDispatchers
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

actual class FinoAppDelegate {
    private val storeConfig = FATStoreConfig().apply {
//        sdkKey = "o4BJLnvnRr4SthikdxriQ1Ndzyt2D/N43waFsQjZ172gA4x+JWh7hhNS5aO52BFs"
//        sdkSecret = "2fab1239365f0ee2"
        sdkKey = "o1h5GoyYuhgVYS8IM9va00te7klU/U02amaoS76ydxs="
        sdkSecret = "1a300007fdd4b768"
        apiServer = "https://api.finclip.com"
        enablePreloadFramework = true
        apiPrefix="/api/v1/mop/"
        cryptType=FATApiCryptType.FATApiCryptTypeSM
    }
    private val config = FATConfig.configWithStoreConfigs(listOf(storeConfig))!!.apply {
        setCurrentUserId("13632830235")
    }

    private lateinit var client:FATClient
    private lateinit var scope: CoroutineScope

    actual suspend fun initEngine(context: PlatformContext, scope: CoroutineScope): Boolean {
        this.scope =scope
        val completer = CompletableDeferred<Boolean>()
        withContext(uoocDispatchers.io) {
            val nsErrorPtr = nativeHeap.alloc<ObjCObjectVar<NSError?>>()
            withContext(uoocDispatchers.main){
                client = FATClient.sharedClient()!!
            }
            val result = client.initWithConfig(config, error = nsErrorPtr.ptr)
            if (nsErrorPtr.value != null) {
                println("FATClient init error:${nsErrorPtr.value!!.localizedDescription}")
                completer.complete(false)
                return@withContext
            }
            println("FATClient init result:$result")
            completer.complete(result)
        }
        return completer.await()
    }

    actual suspend fun openApplet(
        context: PlatformContext,
        appletId: String,
        appletParams: Map<String, String>
    ) {

        val request = FATAppletRequest().apply {
            this.appletId = appletId
            this.apiServer = "https://api.finclip.com"
            this.startParams = appletParams as Map<Any?, *>
        }
        withContext(uoocDispatchers.main){
            (UIApplication.sharedApplication().windows.first() as UIWindow).rootViewController?.apply {
                withContext(uoocDispatchers.io) {
                    client.startAppletWithRequest(request,
                        completion = { result, error ->
                            println("打开小程序:$error result:$result")
                        }, closeCompletion = {
                            println("关闭小程序")
                        }, InParentViewController = this@apply
                    )
                }
            }
        }

    }

    actual suspend fun openLinkApplet(context: PlatformContext, appletLink: String) {
        val qrcodeRequest = FATAppletQrCodeRequest().apply {
            this.qrCode = appletLink
            this.from = NSNumber(10086)
        }
        withContext(uoocDispatchers.main){
            (UIApplication.sharedApplication().windows.first() as UIWindow).rootViewController?.apply {
                withContext(uoocDispatchers.io) {
                    client.startAppletWithQrCodeRequest(
                        qrcodeRequest,
                        inParentViewController = this@apply,
                        requestBlock = { result, error ->
                            println("请求完成：$error result:$result")
                        },
                        completion = { result, error ->
                            println("打开完成：$error result:$result")
                        },
                        closeCompletion = {
                            println("关闭")
                        }
                    )
                }
            }
        }
    }


    actual suspend fun registerAppletHandler(
        method: String,
        handler: suspend (params: Map<String, Any?>?) -> Pair<Boolean, Map<String, Any>>
    ) {
        if(::scope.isInitialized.not())return
        withContext(uoocDispatchers.io) {
            client.registerExtensionApi(method) { appletInfo, param, callback ->
                //param是入参
                //返回成功和出参
                scope.launch {
                    withContext(uoocDispatchers.io) {
                        handler.invoke(param as? Map<String, Any?>).let {
                            println("虾仁：${it.first} ${it.second} $method")
                            if (it.first) {
                                callback?.invoke(FATExtensionCodeSuccess, it.second as Map<Any?, *>)
                            } else {
                                callback?.invoke(FATExtensionCodeFailure, null)
                            }
                        }
                    }
                }
            }
        }
    }

    actual suspend fun destroyAllApplet() {
        withContext(uoocDispatchers.io) {
            client.closeAllAppletsWithCompletion {
                println("关闭所有小程序")
                client.clearLocalApplets()
            }
        }
    }
}
