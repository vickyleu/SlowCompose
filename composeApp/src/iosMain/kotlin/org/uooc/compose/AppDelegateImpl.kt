package org.uooc.compose

import coil3.PlatformContext
import com.rickclephas.kmp.nsexceptionkt.core.InternalNSExceptionKtApi
import com.rickclephas.kmp.nsexceptionkt.core.asNSException
import com.rickclephas.kmp.nsexceptionkt.core.wrapUnhandledExceptionHook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.uooc.compose.base.AppDelegate
import org.uooc.compose.base.BuildConfigImpl
import org.uooc.compose.core.di.initUoocKoin
import org.uooc.compose.utils.Bugly
import org.uooc.compose.utils.thirdparty.UmengUtils
import kotlin.experimental.ExperimentalNativeApi

/**
 * iOS AppDelegate
 */
@Suppress("unused")
class AppDelegateImpl : AppDelegate {
    private var isFirst = true
    private val scope = CoroutineScope(Job())

    override fun onCreate() {
        initUoocKoin(
            enableNetworkLogs = BuildConfigImpl.isDebug,
            platformContext = PlatformContext.INSTANCE
        )
    }

    override fun initPrivacyUtil() {
        if (isFirst) {
            isFirst = false
            scope.launch {
                withContext(Dispatchers.IO) {
                    Bugly.init(PlatformContext.INSTANCE, "2ab2eced8f")
                    println("onPage Bugly.init ??")
                    UmengUtils.instance.initWithAppKey(
                        platformContext = PlatformContext.INSTANCE,
                        appKey = "6715d1b6667bfe33f3c60ea9",
                        channel = BuildConfigImpl.channel
                    )

                    setCrashUnhandledExceptionHook()
                    /*setUnhandledExceptionHook {
                          it.toCleanedException().printStackTrace()
                        processUnhandledException(it)
                    }*/
                }
            }
        }
    }


    @OptIn(InternalNSExceptionKtApi::class)
    public fun setCrashUnhandledExceptionHook(): Unit = wrapUnhandledExceptionHook { throwable ->
        val exception = throwable.asNSException()
        what.the.fuck.Bugly.Bugly.reportException(exception)
    }


    override fun onKillProcess() {
        UmengUtils.instance.onKillProcess(PlatformContext.INSTANCE)
    }

    override fun onDestroy() {
    }

    override fun onStart() {
    }

    override fun onStop() {
    }

    override fun onResume() {
    }

    override fun onPause() {
    }

    override fun onRestart() {
    }

    override fun onLowMemory() {
    }

    override fun onTrimMemory(level: Int) {
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
    }
}
