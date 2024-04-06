package com.marmatsan.dev.core_domain

import kotlin.math.abs
import kotlin.math.log10

val String.Companion.Empty
    inline get() = ""

fun <T> MutableList<T>.toggle(element: T) {
    if (contains(element)) {
        remove(element)
    } else {
        add(element)
    }
}

fun Int.length() = when(this) {
    0 -> 1
    else -> log10(abs(toDouble())).toInt() + 1
}