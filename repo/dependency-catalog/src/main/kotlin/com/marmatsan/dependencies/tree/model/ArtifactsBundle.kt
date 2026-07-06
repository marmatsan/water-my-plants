package com.marmatsan.dependencies.tree.model

/**
 * Represents a Gradle version catalog bundle declaration.
 *
 * A bundle groups multiple [artifacts] under a single [alias], allowing consumers to reference the
 * whole group from `libs.bundles.<alias>`. When [version] is provided, the DSL propagates that same
 * version to the artifacts in the bundle.
 *
 * @property alias Version catalog bundle alias.
 * @property artifacts Artifacts that belong to this bundle.
 * @property version Optional version shared by the bundle artifacts.
 */
data class ArtifactsBundle(
    val alias: String,
    val artifacts: List<Artifact>,
    val version: String? = null
)
