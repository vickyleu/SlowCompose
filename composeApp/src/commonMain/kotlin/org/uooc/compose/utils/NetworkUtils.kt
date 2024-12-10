package org.uooc.compose.utils

import coil3.PlatformContext

interface NetworkUtils {
    fun isNetworkConnected(context:PlatformContext): Boolean
    fun isWifiConnected(context:PlatformContext): Boolean
}


expect class NetworkUtilsImpl() : NetworkUtils{
    override fun isNetworkConnected(context:PlatformContext): Boolean
    override fun isWifiConnected(context:PlatformContext): Boolean
}