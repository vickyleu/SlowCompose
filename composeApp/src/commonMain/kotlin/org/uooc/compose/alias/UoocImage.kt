package org.uooc.compose.alias

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage

@OptIn(ExperimentalCoilApi::class)
@Composable
fun Coil(
    modifier: Modifier, data: Any,
    contentScale: ContentScale = ContentScale.Crop,
    placeHolder: @Composable (() -> Painter)? = null,
    error: @Composable (() -> Painter)? = null,
    colorFilter: ColorFilter? = null,
    contentDescription: String = "${data.hashCode()}"
) {

//    val platform = LocalPlatformContext.current
//    val loader = SingletonImageLoader.get(platform)
//    loader.newBuilder()
//        .eventListener(object : EventListener() {
//
//        })
//        .maxBitmapSize()
    /* val composition by rememberLottieComposition {
         LottieCompositionSpec.JsonString(
             Res.lottieBytes("error_loading")
                 .decodeToString()
         )
     }
     val progress by animateLottieCompositionAsState(
         composition,
         iterations = Compottie.IterateForever,
         isPlaying = true
     )*/

    /*val compositionPlaceholder by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.lottieBytes("placeholder")
                .decodeToString()
        )
    }

    val progressPlaceholder by animateLottieCompositionAsState(
        compositionPlaceholder,
        iterations = Compottie.IterateForever,
        isPlaying = true
    )*/

    var isLoading by remember(data) { mutableStateOf(true) }
    var isError by remember(data) { mutableStateOf(false) }

    BoxWithConstraints(modifier) {
        AsyncImage(
            model = data,
            placeholder = placeHolder?.invoke(),
            error = error?.invoke(),
            contentDescription = contentDescription,
            onLoading = {
                isLoading = true
                isError = false
            },
            onSuccess = {
                isLoading = false
                isError = false
            },
            onError = {
                isLoading = false
                isError = true
            },
            contentScale = contentScale,
            colorFilter = colorFilter,
            modifier = Modifier.matchParentSize(),
        )
        if (isLoading && placeHolder == null) {
            Box(
                modifier.then(
                    Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.6f))
                ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        if (isError && error == null) {
            Box(
                Modifier.matchParentSize().background(Color.White)
                    .border(
                        width = 0.5.dp,
                        color = Color.Black.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(0.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
//                Image(
//                    painter = rememberLottiePainter(
//                        composition = composition,
//                        progress = { progress },
//                    ),
//                    modifier = Modifier.matchParentSize(),
//                    contentDescription = "Lottie animation"
//                )
            }
        }
    }

}

/*
@Composable
fun Kamel(
    modifier: Modifier, data: Any,
    contentScale: ContentScale = ContentScale.Crop,
    placeHolder: @Composable (BoxScope.(Float) -> Unit)? = null,
    error: @Composable (BoxScope.(Throwable) -> Unit)? = null,
    cornerRadius: Dp = 0.dp,
    shape: Shape = RoundedCornerShape(cornerRadius),
    contentDescription: String? = "${data.hashCode()}"
) {
    val coroutineScope = rememberCoroutineScope()
    KamelImage(
        modifier = modifier,
        resource = {
            asyncPainterResource(data)
        },
        contentDescription = contentDescription,
        contentScale = contentScale,
        onLoading = { progress ->
            Box(
                modifier.then(Modifier.background(Color.Black.copy(alpha = 0.6f))),
                contentAlignment = Alignment.Center
            ) {
                if (placeHolder != null) {
                    placeHolder.invoke(this, progress)
                } else {
                    CircularProgressIndicator(
                        progress = { progress },
                    )
                }
            }
        },
        onFailure = { exception ->
            Box(
                modifier.then(
                    Modifier
                        .background(Color.White)
                        .border(
                            width = 0.5.dp,
                            color = Color.Black.copy(alpha = 0.1f),
                            shape = shape
                        )
                ),
                contentAlignment = Alignment.Center
            ) {
                if (error != null) {
                    error.invoke(this, exception)
                } else {
                    val composition by rememberLottieComposition {
                        LottieCompositionSpec.JsonString(
                            Res.lottieBytes("error_loading")
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
                        modifier = Modifier.matchParentSize(),
                        contentDescription = "Lottie animation"
                    )
                }
            }
        }
    )
}*/
