package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiPipeline
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiWindowsRuntime
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig

internal data class CiVisualPlanningContext(
    val externalTopology: CiExternalTopology,
    val windowsRuntime: CiWindowsRuntime,
    val config: CiVisualPlanConfig,
    val ciPipeline: CiPipeline,
    val figmaPipeline: CiPipeline,
)
