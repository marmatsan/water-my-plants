package com.marmatsan.dev.core_ui.util

fun shortSegmentOfGoldenRatio(input: Int): Float {
    return input.toFloat() - input.toFloat() / 1.618f
}

fun shortSegmentOfGoldenRatio(input: Float): Float {
    return input - input / 1.618f
}