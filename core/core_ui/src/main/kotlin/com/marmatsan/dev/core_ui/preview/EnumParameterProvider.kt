package com.marmatsan.dev.core_ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlin.reflect.KClass

abstract class EnumParameterProvider<T : Enum<T>> :
    PreviewParameterProvider<T> {
    abstract val enumClass: KClass<T>
    override val values: Sequence<T>
        get() = enumClass.java.enumConstants?.asSequence() ?: sequenceOf()
}