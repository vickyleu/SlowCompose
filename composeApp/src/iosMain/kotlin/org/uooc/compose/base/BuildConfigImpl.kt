package org.uooc.compose.base

import org.uooc.compose.BuildKonfig
import platform.Foundation.NSProcessInfo

actual object BuildConfigImpl {
    actual val isDebug: Boolean = (NSProcessInfo.processInfo.environment["DEBUG"] == "1")
    actual val generateTime: String
        get() = BuildKonfig.timestamp
    actual val gitCommitHash: String
        get() = BuildKonfig.gitCommitHash
    actual val channel: String
        get() = BuildKonfig.FLAVOR

    actual val isEnableFlexibleHost: Boolean
        get() = BuildKonfig.flexible
    actual val isEnableProxies: Boolean
        get() = BuildKonfig.proxiesDisable
}