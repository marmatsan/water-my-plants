package com.marmatsan.dev.core_ui.svg_package

import androidx.compose.ui.graphics.vector.ImageVector
import com.marmatsan.dev.core_ui.svg_package.svgs.`Illustration=2`
import kotlin.collections.List as ____KtList

public object SVGs

private var __AllSVGs: ____KtList<ImageVector>? = null

public val SVGs.AllSVGs: ____KtList<ImageVector>
  get() {
    if (__AllSVGs != null) {
      return __AllSVGs!!
    }
    __AllSVGs= listOf(`Illustration=2`)
    return __AllSVGs!!
  }
