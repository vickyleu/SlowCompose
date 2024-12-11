package org.uooc.compose.utils.thirdparty

import cocoapods.UMCommon.MobClick
import cocoapods.UMCommon.UMConfigure
import coil3.PlatformContext

actual class UmengUtils {

    private var isInit = false


    actual fun initWithAppKey(
        platformContext: PlatformContext,
        appKey: String,
        channel: String
    ) {
        UMConfigure.initWithAppkey(appKey, channel)
        println("onPage UMConfigure.initWithAppkey($appKey, $channel)")
        isInit = true
    }

    private val pageStack = mutableListOf<String>()

    fun onKillProcess(platformContext: PlatformContext) {
    }

    actual fun onPageStart(pageName: String) {
        if (isInit.not()) return
        // 如果栈顶的页面还未调用结束，就先结束栈顶页面
        if (pageStack.isNotEmpty() && pageStack.last() != pageName) {
            onPageEnd(pageStack.last())
        }
//        println("onPageStart, pageName: $pageName")
        pageStack.add(pageName)
        MobClick.beginLogPageView(pageName)
    }

    actual fun onPageEnd(pageName: String) {
        if (isInit.not()) return
        // 栈中没有该页面或栈顶不是该页面，返回不做处理
        if (pageStack.isEmpty() || pageStack.last() != pageName) return
//        println("onPageEnd, pageName: $pageName")
        pageStack.removeLast()
        MobClick.endLogPageView(pageName)
    }

    actual fun onEvent(platformContext: PlatformContext, eventId: String) {
        if (isInit.not()) return
        MobClick.event(eventId)
    }

    actual fun onEvent(platformContext: PlatformContext, eventId: String, label: String) {
        if (isInit.not()) return
        MobClick.event(eventId, label)
    }

    actual fun onEvent(
        platformContext: PlatformContext,
        eventId: String,
        map: Map<Any?, String>
    ) {
        if (isInit.not()) return
        MobClick.event(eventId, attributes = map)
    }

    actual companion object {
        private var _instance: UmengUtils? = null
        actual val instance: UmengUtils
            get() = _instance ?: UmengUtils().also { _instance = it }

        actual fun preInit(
            platformContext: PlatformContext,
            appKey: String,
            channel: String
        ) {

        }
    }

}