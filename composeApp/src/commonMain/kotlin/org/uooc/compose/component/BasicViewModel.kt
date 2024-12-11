package org.uooc.compose.component

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import coil3.PlatformContext
import com.uooc.annotation.ModuleAssemble
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.koin.core.component.KoinComponent
import org.uooc.compose.core.uoocDispatchers
import org.uooc.compose.resources.Res
import org.uooc.compose.resources.ic_arrow_back
import org.uooc.compose.utils.NetworkUtils
import kotlin.jvm.Transient


data class TitleInfo(
    val titleCenterPosition: Offset,
    val titleStyle: TextStyle,
)


enum class ScreenState{
    INIT,
    LOADING,
    LOADED,
    ERROR,
    EMPTY
}

@ModuleAssemble
abstract class BasicViewModel : ScreenModel, KoinComponent {

    internal var screenState = mutableStateOf(ScreenState.INIT)
        private set

    open val useFullLoading: Boolean
        get() = false

    open val topBarColor: Color
        get() = Color.White

    open val haveStatusDivider: Boolean
        get() = false

    open val showingTitle: Boolean
        get() = true

    open val naviBarColor: Color
        get() = Color.White

    open val hasTopBar: Boolean
        get() = true

    open val arrowBackIcon: DrawableResource
        get() = Res.drawable.ic_arrow_back

    open val tintColor: Color
        get() = Color.Transparent

    open val titleColor: Color
        get() = Color.Black

    val networkUtil by lazy { getKoin().get<NetworkUtils>() }


    open val title: String
        get() = this::class.simpleName!!
            .replace("viewModel", "")
            .replace("viewmodel", "")
            .replace("ViewModel", "")

    private val isFirstLoading = mutableStateOf(true)

    internal fun setScreenState(state: ScreenState=ScreenState.LOADED){
        screenState.value = state
    }
    private var _isTransitionAnimationEnable = true
    fun animationTransitionDisable(animationTime:Int = this.animationTime,callback: () -> Unit) {
        screenModelScope.launch {
            _isTransitionAnimationEnable = false
            withContext(uoocDispatchers.main) {
                callback()
            }
            withContext(uoocDispatchers.io) {
                delay(animationTime.toLong())
                _isTransitionAnimationEnable = true
            }
        }
    }
    open fun transitionAnimationEnable():Boolean{
        return _isTransitionAnimationEnable
    }

    internal fun showToast(context: PlatformContext, errorMessage: String){
//        screenModelScope.launch {
//            withContext(uoocDispatchers.main) {
//                context.showToast(errorMessage)
//            }
//        }
    }
    open val animationTime = 500


    open fun shouldRePrepare(): Boolean {
        if(isFirstLoading.value){
            isFirstLoading.value = false
            if(screenState.value == ScreenState.INIT){
                screenState.value = ScreenState.LOADING
            }
           return true
        }
        return false
    }

    val titleInfo = mutableStateOf(
        TitleInfo(
            Offset.Unspecified,
            TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )
    )

    open suspend fun backCallBack(): Boolean {
        return true
    }

    @Transient
    private val backPressed = derivedStateOf { ::backCallBack }


    suspend fun onBackPressed(): Boolean {
        return backPressed.value.invoke()
    }

    open fun prepare() {

    }
    open fun recycle() {

    }

    final override fun onDispose() {
        recycle()
//        println("navigator model this::${this::class.simpleName}")
    }


    open fun <T : BasicViewModel> injectScreen(screen: BasicScreen<T>, scope: CoroutineScope,depth:Int) {

    }

    open fun <T : BasicViewModel> dejectScreen(screen: BasicScreen<T>, scope: CoroutineScope,depth:Int) {

    }

    internal fun userStateChange(hasUserInfo: Boolean){
        isFirstLoading.value = true
    }


}


abstract class BasicTabViewModel : BasicViewModel() {
    override val hasTopBar: Boolean
        get() = false
    override val topBarColor: Color
        get() = Color.Transparent
    override val showingTitle: Boolean
        get() = false

}

abstract class BasicStatelessViewModel : BasicViewModel() {
    override val hasTopBar: Boolean
        get() = false
    override val topBarColor: Color
        get() = Color.Transparent
    override val showingTitle: Boolean
        get() = false
}