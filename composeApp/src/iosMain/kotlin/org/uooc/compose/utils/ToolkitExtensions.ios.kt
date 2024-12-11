package org.uooc.compose.utils

import coil3.PlatformContext
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.convert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.uooc.compose.network.trustIsValid
import platform.CoreFoundation.CFArrayRef
import platform.Foundation.NSURLAuthenticationMethodServerTrust
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLProtectionSpace
import platform.Foundation.NSURLSessionAuthChallengeCancelAuthenticationChallenge
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.Foundation.credentialForTrust
import platform.Foundation.serverTrust
import platform.Security.SecTrustCopyCertificateChain
import platform.Security.SecTrustRef
import platform.Security.SecTrustSetAnchorCertificates
import kotlin.system.exitProcess

actual fun killApp(context: PlatformContext) {
    exitProcess(0)
}

actual fun isKeyboardVisible(platformContext: PlatformContext): Boolean {
    return true
}

/**
 * ios 使用curl将日志发送到charles
 */
actual fun printlnFix(message: String) {
    println(message)
//    sendLogToCharles(message)
}

private val logClient = HttpClient(Darwin) {
    expectSuccess = false // 允许非 2xx 响应
    engine {
        handleChallenge { session, task, challenge, completionHandler ->
            autoreleasepool {
                var serverTrust: SecTrustRef? = null
                var certChain: CFArrayRef? = null
                try {
                    // 检查是否为 Server Trust 验证类型
                    val protectionSpace: NSURLProtectionSpace = challenge.protectionSpace
                    if (protectionSpace.authenticationMethod != NSURLAuthenticationMethodServerTrust) {
                        completionHandler(
                            NSURLSessionAuthChallengePerformDefaultHandling.convert(),
                            null
                        )
                        return@handleChallenge
                    }
                    serverTrust = protectionSpace.serverTrust
                    if (serverTrust == null) {
                        completionHandler(
                            NSURLSessionAuthChallengePerformDefaultHandling.convert(),
                            null
                        )
                        return@handleChallenge
                    }
                    // 获取服务器证书链
                    certChain = SecTrustCopyCertificateChain(serverTrust)
                    // 设置证书链为可信锚点
                    SecTrustSetAnchorCertificates(serverTrust, certChain)
                    if (serverTrust.trustIsValid()) {
                        // 服务器证书有效，继续
                        val credential = NSURLCredential.credentialForTrust(serverTrust)
                        completionHandler(
                            NSURLSessionAuthChallengeUseCredential.convert(),
                            credential
                        )
                    } else {
                        // 服务器证书无效，取消认证
                        completionHandler(
                            NSURLSessionAuthChallengeCancelAuthenticationChallenge.convert(),
                            null
                        )
                    }
                } finally {
                    // 确保释放 Core Foundation 对象
                    /*certChain?.let { CFRelease(it) }
                    serverTrust?.let { CFRelease(it) }*/
                }
            }
        }
        configureSession {
            this.connectionProxyDictionary = mapOf<Any?, Any>(
                "HTTPEnable" to 1,
                "HTTPSEnable" to 1,
                "HTTPProxy" to "192.168.2.89",
                "HTTPPort" to 8888,
                "HTTPSProxy" to "192.168.2.89",
                "HTTPSPort" to 8888,
            )
            this.allowsCellularAccess = true
        }
    }
    install(HttpRequestRetry) {
        noRetry()
    }
}
val scope = CoroutineScope(Dispatchers.Default)
fun sendLogToCharles(logMessage: String) {
    scope.launch {
        withContext(Dispatchers.Default) {
            try {
                logClient.post("https://www.baidu.com/log") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"log": "$logMessage"}""")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}