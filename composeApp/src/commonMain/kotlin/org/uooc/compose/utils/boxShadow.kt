package org.uooc.compose.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.lennartegb.shadows.boxShadow

data class ShadowValues(
    val blurRadius: Dp,
    val color: Color,
    val spreadRadius: Dp,
    val offset: DpOffset,
    val shape: Shape,
    val clip: Boolean,
    val inset: Boolean,
)

val DefaultShadowValues = ShadowValues(
    blurRadius = 0.dp,
    color = Color.Black,
    spreadRadius = 0.dp,
    offset = androidx.compose.ui.unit.DpOffset.Zero,
    shape = RectangleShape,
    clip = false,
    inset = false,
)

fun Modifier.boxShadow(density: Density, shadowValues: ShadowValues) = boxShadow(
    density = density,
    blurRadius = shadowValues.blurRadius,
    color = shadowValues.color,
    spreadRadius = shadowValues.spreadRadius,
    offset = shadowValues.offset,
    shape = shadowValues.shape,
    clip = shadowValues.clip,
    inset = shadowValues.inset,
)