package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Repository-owned visual target and its path inside the portable design model. */
data class FigmaCatalogTreeTargetConfig(
    val name: String,
    val sectionNodeId: String,
    val type: FigmaCatalogTreeTargetType,
    val lifecycle: FigmaCatalogTreeTargetLifecycle,
    val nodesPath: List<String>,
    val gradlePluginNodes: Boolean = false,
    val warnWhenUnused: Boolean = false
)
