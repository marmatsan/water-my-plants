package com.marmatsan.dependencies.tree.model

/**
 * Represents a single Maven artifact within a library group.
 *
 * @property artifact The artifact identifier, usually matching Maven `artifactId`.
 * @property version Optional version for the artifact. When absent, version may be provided
 * by a containing [ArtifactsBundle] or an external resolution mechanism.
 */
data class Artifact(
    val artifact: String,
    val version: String? = null
)
