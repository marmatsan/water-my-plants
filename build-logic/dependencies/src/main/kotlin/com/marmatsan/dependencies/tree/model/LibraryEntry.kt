package com.marmatsan.dependencies.tree.model

/**
 * Represents one catalog entry declared under a library group.
 *
 * A library group can register individual artifacts via [Single] and version catalog bundles via
 * [Bundle]. The version catalog registration layer interprets these entries and creates the
 * corresponding aliases and bundles.
 */
sealed interface LibraryEntry {

    /**
     * An entry that registers one artifact alias under the current library group.
     *
     * @property artifact Artifact to register.
     */
    data class Single(
        val artifact: Artifact
    ) : LibraryEntry

    /**
     * An entry that registers multiple artifacts and groups them under a bundle alias.
     *
     * @property artifactsBundle Bundle to register.
     */
    data class Bundle(
        val artifactsBundle: ArtifactsBundle
    ) : LibraryEntry
}
