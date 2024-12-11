package org.uooc.compose.base

expect interface AppDelegate {
    fun initPrivacyUtil()
    fun onCreate()
    fun onDestroy()
    fun onStart()
    fun onStop()
    fun onResume()
    fun onPause()
    fun onRestart()
    fun onLowMemory()
    fun onTrimMemory(level: Int)
    fun onWindowFocusChanged(hasFocus: Boolean)
    fun onKillProcess()
}

