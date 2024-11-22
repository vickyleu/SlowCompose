package chaintech.videoplayer.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.request.ImageRequest

@Composable
fun ImageFromUrl(
    modifier: Modifier,
    data: Any,
    contentScale: ContentScale = ContentScale.Crop,
) {
    Box(modifier, Alignment.Center) {
        /*val request = remember(data, 0, true) {
            ImageRequest {
                data(data)
                options {
                    playAnimate = true
                }
            }
        }
        val action by rememberImageAction(request)
        val painter = rememberImageActionPainter(action)*/
        AsyncImage(
            model=data,
            contentDescription = "",
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
        )
    }
}