package org.uooc.compose.base

import coil3.PlatformContext
import kotlinx.coroutines.CoroutineScope

expect class FinoAppDelegate() {
    suspend fun initEngine(context: PlatformContext, scope: CoroutineScope): Boolean

    suspend fun openApplet(
        context: PlatformContext,
        appletId: String,
        appletParams: Map<String, String>
    )

    suspend fun openLinkApplet(context: PlatformContext, appletLink: String)

    suspend fun registerAppletHandler(
        method: String,
        handler: suspend (params: Map<String, Any?>?) -> Pair<Boolean, Map<String, Any>>
    )

    suspend fun destroyAllApplet()
}
