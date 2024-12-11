package org.uooc.compose.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.ScreenTransition
import coil3.compose.LocalPlatformContext
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.uooc.compose.base.getPlatform
import org.uooc.compose.core.uoocDispatchers
import org.uooc.compose.resources.Res
import org.uooc.compose.utils.LocalNavigatorController
import org.uooc.compose.utils.lottieBytes
import org.uooc.compose.utils.thirdparty.UmengUtils
import kotlin.jvm.Transient

expect interface BasicScreenSerializer

expect interface TabImpl : Tab

@OptIn(ExperimentalVoyagerApi::class, InternalVoyagerApi::class)

abstract class BasicScreen<T : BasicViewModel>(
    @Transient private val create: Koin.() -> T,
    @Transient private val screenDepth: T.(screen: BasicScreen<T>) -> Int = { 0 },
) : TabImpl, KoinComponent, BasicScreenSerializer, ScreenTransition {

    @delegate:Transient
    internal val model: T by lazy {
        create(getKoin())
    }
    override val key: ScreenKey = uniqueScreenKey



    open val rootColor: Color
        @Composable
        get() = Color.White//MaterialTheme.colorScheme.background


    @OptIn(InternalVoyagerApi::class)
    @Suppress("UNCHECKED_CAST")
    @Composable
    final override fun Content() {
        val scope = rememberCoroutineScope()
        val platform = LocalPlatformContext.current
        val screenModel: BasicViewModel =
            rememberScreenModel<BasicViewModel>(
                tag = try {
                    "${model.hashCode()}:${model::class.qualifiedName}${
                        with(model) {
                            screenDepth(this@BasicScreen)
                        }
                    }"
                } catch (ignored: Exception) {
                    "ignored"
                }
            ) {
                model.networkUtil.isWifiConnected(platform)
                model.apply {
                    val depth = screenDepth(this@BasicScreen)
                    model.injectScreen(this@BasicScreen, scope, depth)
                }
            }

        

        val navigator = navigator
        DisposableEffect(Unit) {
            if (model.shouldRePrepare()) {
                prepare(screenModel as T, navigator = navigator)
            }
            onPageStart()
            onDispose {
//                println("navigator dispose ${this@BasicScreen::class.simpleName}")
                println("dispose ${this@BasicScreen::class.simpleName}")
                onPageEnd()
            }
        }
        with(LocalDensity.current) {

            val topAppBarHeightAssign = remember { mutableStateOf(0.dp) }
            Scaffold(
                modifier = Modifier.fillMaxSize().background(Color.Transparent),
                contentColor = Color.Transparent,
                containerColor = rootColor,
                topBar = {
                    
                },
                content = {
                    val tabbarHeight = topAppBarHeightAssign.value
                    BoxWithConstraints(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (model.screenState.value == ScreenState.LOADING && model.useFullLoading) {
                            val screenWidth = getPlatform().screenSize().width.toDp()
                            BoxWithConstraints(
                                Modifier
                                    .let {
                                        if (model.hasTopBar) {
                                            it.padding(top = tabbarHeight)
                                        } else it
                                    }
                                    .padding(calculatePaddingValue())
                                    .fillMaxSize()
                                    .background(Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF000000).copy(alpha = 0.68f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clip(
                                            RoundedCornerShape(6.dp)
                                        )
                                        .requiredSizeIn(
                                            minWidth = 126.dp,
                                            minHeight = 20.dp,
                                            maxWidth = screenWidth * 0.7f,
                                            maxHeight = 280.dp
                                        )
                                        .wrapContentSize()
                                        .padding(horizontal = 21.dp, vertical = 15.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .wrapContentSize(),
                                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val composition by rememberLottieComposition {
                                            LottieCompositionSpec.JsonString(
                                                Res.lottieBytes("progressive_loading")
                                                    .decodeToString()
                                            )
                                        }
                                        val progress by animateLottieCompositionAsState(
                                            composition,
                                            iterations = Compottie.IterateForever,
                                            isPlaying = true
                                        )
                                        Image(
                                            painter = rememberLottiePainter(
                                                composition = composition,
                                                progress = { progress },
                                            ),
                                            modifier = Modifier.size(30.dp),
                                            contentDescription = "Lottie animation"
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "加载中...",
                                            fontSize = 16.sp,
                                            overflow = TextOverflow.Ellipsis,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        } else {
                            modelContent(screenModel as T, navigator, tabbarHeight)
                        }
                    }
                },
                contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(
                    WindowInsets.ime
                ),
                bottomBar = {
                    modelBottomBar(screenModel as T, navigator)
                }
            )
        }
    }


    open fun onPageStart() {
        UmengUtils.instance.onPageStart(this::class.simpleName ?: "")
    }

    open fun onPageEnd() {
        UmengUtils.instance.onPageEnd(this::class.simpleName ?: "")
    }

    @Composable
    open fun calculatePaddingValue(): PaddingValues {
        return PaddingValues(0.dp)
    }

    open fun prepare(model: T, navigator: Navigator) {
        model.prepare()
    }


    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 0u, title = "${this::class.simpleName}")


    @Composable
    private fun actionsRowScope(model: T, scope: RowScope) {
        scope.actions(model, navigator)
    }

    @Composable
    open fun RowScope.actions(model: T, navigator: Navigator) {

    }

    @Composable
    open fun modelTopBar(
        model: T,
        navigator: Navigator,
        topAppBarHeightAssign: MutableState<Dp>
    ) {
        val scope = rememberCoroutineScope()
        BasicTopbar(
            onBack = {
                if (navigator.canPop) {
                    scope.launch {
                        val agree = onBackPressed(model, navigator)
                        if (agree) {
                            model.apply {
                                val depth = screenDepth(this@BasicScreen)
                                model.dejectScreen(this@BasicScreen, scope, depth)
                            }
                            navigator.pop()
                        }
                    }
                }
            },
            forceShowBack = forceShowingBack(),
            model = model,
            action = {
                actionsRowScope(model, this)
            },
            navigator = navigator,
            topAppBarHeightAssign = topAppBarHeightAssign
        )
    }

    internal open fun forceShowingBack(): Boolean {
        return false
    }

    @Composable
    abstract fun modelContent(model: T, navigator: Navigator, tabbarHeight: Dp)

    @Composable
    open fun modelBottomBar(model: T, navigator: Navigator) {
    }

    @Composable
    open fun canPop(): Boolean {
        return navigator.canPop
    }

    open fun recycle() {
        model.recycle()
    }


    open suspend fun onBackPressed(model: T, navigator: Navigator): Boolean {
        return model.onBackPressed()
    }

    @Transient
    private var _isTransitionOpen: Boolean = model.transitionAnimationEnable()


    override fun enter(lastEvent: StackEvent): EnterTransition? {
        return (if (try {
                _isTransitionOpen
            } catch (e: Exception) {
                false
            }
        ) slideInHorizontally(
            animationSpec = tween(model.animationTime),
            initialOffsetX = {
                it
            }
        ) + fadeIn(
            animationSpec = tween(model.animationTime)
        ) else null)
    }


    @Transient
    private val isInTransition = mutableStateOf(false)

    override fun exit(lastEvent: StackEvent): ExitTransition? {
        return (if (try {
                _isTransitionOpen
            } catch (e: Exception) {
                false
            }
        ) {
            (slideOutHorizontally(
                animationSpec = tween(500),
                targetOffsetX = {
                    it
                }
            ) + fadeOut(
                animationSpec = tween(500)
            ))
        } else null)
    }

    final fun transitionAnimation(enable: Boolean) {
        _isTransitionOpen = enable
    }


}

expect annotation class ParcelizeImpl()


abstract class BasicStatelessScreen<T : BasicStatelessViewModel>(@Transient val create: Koin.() -> T) :
    BasicScreen<T>(create) {
    @Composable
    final override fun modelBottomBar(model: T, navigator: Navigator) {
    }

    @Composable
    final override fun modelTopBar(
        model: T,
        navigator: Navigator,
        topAppBarHeightAssign: MutableState<Dp>
    ) {
        if (model.hasTopBar) {
            super.modelTopBar(model, navigator, topAppBarHeightAssign)
        }
    }


    // FakeOverride fix for https://youtrack.jetbrains.com/issue/CMP-6842
    final override val options: TabOptions
        @Composable
        get() = super.options

    override val rootColor: Color
        @Composable
        get() = Color.Transparent

    @Composable
    final override fun canPop(): Boolean {
        return super.canPop()
    }

    @Composable
    override fun calculatePaddingValue(): PaddingValues {
        return super.calculatePaddingValue()
    }

    @Composable
    override fun RowScope.actions(model: T, navigator: Navigator) {
    }

    @Composable
    override fun modelContent(model: T, navigator: Navigator, tabbarHeight: Dp) {
    }
}

abstract class BasicTab<T : BasicTabViewModel>(@Transient val create: Koin.() -> T) :
    BasicScreen<T>(create) {
    @Suppress("UNCHECKED_CAST")
    override val options: TabOptions
        @Composable
        get() {
            val screenModel =
                rememberScreenModel<ScreenModel>(tag = "tab=${model.hashCode()}:${model::class.qualifiedName}") { model }
            return modelOption(screenModel as T, tabNavigator)
        }

    @Composable
    final override fun modelBottomBar(model: T, navigator: Navigator) {
    }

    @Composable
    final override fun modelTopBar(
        model: T,
        navigator: Navigator,
        topAppBarHeightAssign: MutableState<Dp>
    ) {
        if (model.hasTopBar) {
            super.modelTopBar(model, navigator, topAppBarHeightAssign)
        }
    }


    @Composable
    abstract fun modelOption(model: T, tabNavigator: Navigator): TabOptions

    // FakeOverride fix for https://youtrack.jetbrains.com/issue/CMP-6842
    override val rootColor: Color
        @Composable
        get() = Color.Transparent

    @Composable
    final override fun canPop(): Boolean {
        return super.canPop()
    }

    @Composable
    override fun calculatePaddingValue(): PaddingValues {
        return super.calculatePaddingValue()
    }

    @Composable
    override fun RowScope.actions(model: T, navigator: Navigator) {
    }

    @Composable
    override fun modelContent(model: T, navigator: Navigator, tabbarHeight: Dp) {
    }
}

private val BasicScreen<*>.navigator: Navigator
    @Composable
    get() = LocalNavigatorController.current


private val BasicScreen<*>.tabNavigator: Navigator
    @Composable
    get() = LocalNavigator.current!!


