package org.uooc.compose.utils

import cocoapods.AFNetworking.AFNetworkReachabilityManager
import cocoapods.AFNetworking.AFNetworkReachabilityStatus
import cocoapods.AFNetworking.AFNetworkReachabilityStatusNotReachable
import cocoapods.AFNetworking.AFNetworkReachabilityStatusReachableViaWWAN
import cocoapods.AFNetworking.AFNetworkReachabilityStatusReachableViaWiFi
import coil3.PlatformContext
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityFlags
import platform.SystemConfiguration.SCNetworkReachabilityFlagsVar
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionRequired
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsReachable

actual class NetworkUtilsImpl : NetworkUtils {
    // 创建 AFNetworkReachabilityManager 实例
    private val reachabilityManager = AFNetworkReachabilityManager.sharedManager()

    private var currentStatus: AFNetworkReachabilityStatus = AFNetworkReachabilityStatusNotReachable

    private fun getNetworkStatus(): SCNetworkReachabilityFlags? {
        val reachability =
            SCNetworkReachabilityCreateWithName(null, "www.baijia.com") ?: return null
        memScoped {
            val flags = nativeHeap.alloc<SCNetworkReachabilityFlagsVar>()
            try {
                val success = SCNetworkReachabilityGetFlags(reachability, flags.ptr)
                if (success) {
                    return flags.value
                }
            } catch (e: Exception) {
                nativeHeap.free(flags.ptr)
            }
        }
        return null
    }

    init {
        reachabilityManager.setReachabilityStatusChangeBlock {
            currentStatus = it
        }
        reachabilityManager.startMonitoring()
        currentStatus = when {
            reachabilityManager.isReachableViaWiFi() -> {
                AFNetworkReachabilityStatusReachableViaWiFi
            }

            reachabilityManager.isReachableViaWWAN() -> {
                AFNetworkReachabilityStatusReachableViaWWAN
            }

            else -> {
                AFNetworkReachabilityStatusNotReachable
            }
        }
        if (currentStatus == AFNetworkReachabilityStatusNotReachable) {
            getNetworkStatus()?.let {
                currentStatus =
                    if (it and kSCNetworkReachabilityFlagsReachable != 0u && it and kSCNetworkReachabilityFlagsConnectionRequired == 0u) {
                        AFNetworkReachabilityStatusReachableViaWiFi // 或其他判断逻辑
                    } else {
                        AFNetworkReachabilityStatusNotReachable
                    }
            }
        }
    }

    actual override fun isNetworkConnected(context: PlatformContext): Boolean {
        val isNetwork: Boolean = when (currentStatus) {
            AFNetworkReachabilityStatusReachableViaWiFi,
            AFNetworkReachabilityStatusReachableViaWWAN -> {
                // 网络已连接
                true
            }

            else -> {
                // 网络未连接
                false
            }
        }
        return isNetwork
    }

    actual override fun isWifiConnected(context: PlatformContext): Boolean {
        val isWifi: Boolean = when (currentStatus) {
            AFNetworkReachabilityStatusReachableViaWiFi -> {
                // Wi-Fi 已连接
                true
            }

            else -> {
                // Wi-Fi 未连接
                false
            }
        }
        return isWifi
    }
}