package com.marmatsan.dependencies.tree.model

/**
 * Represents one artifact entry inside a library group.
 *
 * The group is not stored here. It is provided by the surrounding [Dependency.Library] or
 * [DependencyNode.Library] path when the tree is mapped to catalog dependencies.
 *
 * @property artifact The artifact identifier, usually matching Maven `artifactId`.
 * @property version Optional artifact version. When `null`, the version is expected to be provided
 * by a bundle, a BOM, a version catalog constraint, or another external resolution mechanism.
 */
data class Artifact(
    val artifact: String,
    val version: String? = null
)
