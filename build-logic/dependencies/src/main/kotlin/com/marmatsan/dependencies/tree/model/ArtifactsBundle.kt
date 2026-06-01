package com.marmatsan.dependencies.tree.model

/**
 * Represents a named collection of artifacts.
 *
 * Bundles are addressed by an [alias] and contain multiple [artifacts]. A bundle can optionally
 * provide a [version] that can act as a common/default version for the artifacts it contains,
 * depending on your resolution rules.
 *
 * @property alias Logical name used to reference this bundle.
 * @property artifacts The artifacts that belong to this bundle.
 * @property version Optional version shared by the bundle. Interpretation (default vs enforced)
 * depends on the resolution strategy.
 */
data class ArtifactsBundle(
    val alias: String,
    val artifacts: List<Artifact>,
    val version: String? = null
)
