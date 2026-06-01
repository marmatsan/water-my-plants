package com.marmatsan.dependencies.tree.model

/**
 * Represents an entry within a [Dependency.Library].
 *
 * Entries are either:
 * - [Single]: a single [Artifact]
 * - [Bundle]: a bundle of artifacts ([ArtifactsBundle])
 */
sealed interface LibraryEntry {

    /**
     * A library entry representing a single artifact.
     *
     * @property artifact The artifact definition.
     */
    data class Single(
        val artifact: Artifact
    ) : LibraryEntry

    /**
     * A library entry representing an artifacts bundle.
     *
     * @property artifactsBundle The bundle definition.
     */
    data class Bundle(
        val artifactsBundle: ArtifactsBundle
    ) : LibraryEntry
}
