package com.marmatsan.dev.core_ui.extension

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier

fun Double.shortSegmentOfGoldenRatio(): Float {
    return this.toFloat() / 1.618f
}

fun Int.shortSegmentOfGoldenRatio(): Float {
    return this.toFloat() / 1.618f
}

context (ColumnScope) fun Modifier.fillAvailableSpace() = this
    .fillMaxWidth()
    .weight(1f)
context (RowScope) fun Modifier.fillAvailableSpace() = this
    .fillMaxWidth()
    .weight(1f)