package org.uooc.compose.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.uooc.compose.utils.HtmlFormater

data class RichTextCompose(
    private var text: String,
    val clickImages: (images: List<String>, currentIndex: Int) -> Unit = { _, _ -> },
    val clickUrl: (url: String) -> Unit = {},
) {

    val content: String by lazy {
        HtmlFormater.replaceLatexHtmlContent(text)
    }

    var isFillWidth: Boolean = true
    var needMeasure:Boolean = true

    val width: MutableState<Dp> = mutableStateOf(0.dp)
    val height: MutableState<Dp> = mutableStateOf(0.dp)
}

fun convertToLatex(input: String): String {
    return if (input.contains("backslash ")) {
        input.replace("\\backslash ", "\\")
//                .replace("rightarrow ", " > ")
//                .replace("leftarrow ", " < ")
            .replace("\\{", "{").replace("\\}", "}").replace("\\~", "~")
    } else input

}

fun convertImageSrc(input: String): String {
    val regex = """src="data:image/\w+?;base64,(.*?)"""".toRegex()
    return regex.replace(input) { matchResult ->
        val base64Data = matchResult.groupValues[1]
        """src="^data:image/png;base64,$base64Data""""
    }
}

@Composable
expect fun RichTextPlatformView(
    state: RichTextCompose,
    style: TextStyle,
    modifier: Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color = Color.Transparent,
)

