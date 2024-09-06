package com.marmatsan.dev.core_domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

val String.Companion.Empty
    inline get() = ""

fun <T> MutableList<T>.toggle(element: T) {
    if (contains(element)) {
        remove(element)
    } else {
        add(element)
    }
}

val Int.length: Int
    inline get() = this.toString().length

inline fun <T, R> T?.checkIfNull(
    ifNull: () -> R,
    ifNotNull: (T) -> R
): R = when (this) {
    null -> ifNull()
    else -> ifNotNull(this)
}

@OptIn(ExperimentalContracts::class)
fun <T> T?.isNull(): Boolean {
    contract {
        returns(value = true) implies (this@isNull == null)
        returns(value = false) implies (this@isNull != null)
    }
    return this == null
}

@OptIn(ExperimentalContracts::class)
fun <T> T?.isNotNull(): Boolean {
    contract {
        returns(value = true) implies (this@isNotNull != null)
        returns(value = false) implies (this@isNotNull == null)
    }
    return this != null
}

inline fun <T> MutableStateFlow<T>.updateState(update: T.() -> T) {
    this.value = this.value.update()
}