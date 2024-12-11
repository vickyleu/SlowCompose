package org.uooc.compose.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import com.seiko.compose.hellorust.HelloRust

class Greeting private constructor() {
    private val platform = getPlatform()

    @Composable
    fun screenSize():Size{
        return platform.screenSize()
    }
    fun greet(): String {
        return "Hello,! ${platform.name}  ${HelloRust.hello()}" //
    }



    companion object{
        private var _instance:Greeting? = null
        val instance:Greeting
            get() {
                if(_instance == null){
                    _instance = Greeting()
                }
                return _instance!!
            }

    }
}