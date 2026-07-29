package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Repository-owned visual target and its path inside the portable design model.
 *
 * @property name stable visual target name.
 * @property sectionNodeId Figma section updated for this target.
 * @property type visual node family rendered for the catalog.
 * @property lifecycle behavior when the catalog is absent from the model.
 * @property nodesPath path to catalog nodes inside the design model.
 * @property gradlePluginNodes whether entries represent Gradle convention plugins.
 * @property warnWhenUnused whether unused catalog declarations should produce warnings.
 * @property versionValuesPath optional path to resolved version values used to
 * explain version references in plugin nodes.
 * @property sharedVersionKeys version references governed by one coordinated
 * release train.
 */
data class FigmaCatalogTreeTargetConfig(
    val name: String,
    val sectionNodeId: String,
    val type: FigmaCatalogTreeTargetType,
    val lifecycle: FigmaCatalogTreeTargetLifecycle,
    val nodesPath: List<String>,
    val gradlePluginNodes: Boolean = false,
    val warnWhenUnused: Boolean = false,
    val versionValuesPath: List<String> = emptyList(),
    val sharedVersionKeys: List<String> = emptyList()
)
