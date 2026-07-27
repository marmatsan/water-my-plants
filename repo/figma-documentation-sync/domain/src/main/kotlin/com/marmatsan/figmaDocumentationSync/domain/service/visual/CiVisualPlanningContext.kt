package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiPipeline
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiWindowsRuntime
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig

/**
 * Validated input shared by independently extensible CI section planners.
 *
 * @property externalTopology versioned external systems and connections.
 * @property windowsRuntime versioned Windows service topology.
 * @property config project-owned visual names and source links.
 * @property ciPipeline effective primary CI pipeline.
 * @property figmaPipeline effective Figma publication pipeline.
 */
internal data class CiVisualPlanningContext(
    val externalTopology: CiExternalTopology,
    val windowsRuntime: CiWindowsRuntime,
    val config: CiVisualPlanConfig,
    val ciPipeline: CiPipeline,
    val figmaPipeline: CiPipeline,
)
