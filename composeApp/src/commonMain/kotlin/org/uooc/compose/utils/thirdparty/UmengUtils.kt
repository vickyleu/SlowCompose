package org.uooc.compose.utils.thirdparty

import cafe.adriel.voyager.core.stack.Stack
import cafe.adriel.voyager.core.stack.StackEvent
import coil3.PlatformContext

expect class UmengUtils private constructor() {


    fun initWithAppKey(
        platformContext: PlatformContext,
        appKey: String,
        channel: String
    )

    fun onPageStart(pageName: String)
    fun onPageEnd(pageName: String)
    fun onEvent(platformContext: PlatformContext, eventId: String)
    fun onEvent(platformContext: PlatformContext, eventId: String, label: String)
    fun onEvent(platformContext: PlatformContext, eventId: String, map: Map<Any?, String>)

    companion object {
        val instance: UmengUtils
        fun preInit(platformContext: PlatformContext, appKey: String, channel: String)
    }

}