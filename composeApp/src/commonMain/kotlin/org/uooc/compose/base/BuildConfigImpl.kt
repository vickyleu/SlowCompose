package org.uooc.compose.base

expect object BuildConfigImpl{
    val isDebug:Boolean

    val generateTime:String

    val gitCommitHash:String

    val channel:String

    val isEnableFlexibleHost:Boolean

    val isEnableProxies:Boolean
}