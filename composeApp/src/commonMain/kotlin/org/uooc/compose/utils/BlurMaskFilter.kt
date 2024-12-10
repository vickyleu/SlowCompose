package org.uooc.compose.utils

import androidx.compose.ui.graphics.NativePaint

internal expect fun NativePaint.setMaskFilter(blurRadius: Float)
@Suppress("SHADOWED_BY_OTHER_MEMBER")
internal expect var NativePaint.color: Int

