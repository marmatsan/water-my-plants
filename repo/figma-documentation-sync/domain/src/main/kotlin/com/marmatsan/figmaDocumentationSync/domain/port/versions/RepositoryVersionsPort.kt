package com.marmatsan.figmaDocumentationSync.domain.port.versions

import com.marmatsan.figmaDocumentationSync.domain.model.versions.RepositoryVersionSection

/**
 * Port for reading repository version declarations from a version source.
 *
 * Implementations read the configured repository versions file and expose both a flat
 * key/value map and ordered [RepositoryVersionSection] values for Figma
 * documentation.
 *
 * @sample com.marmatsan.figmaDocumentationSync.domain.samples.DomainKDocSamples.repositoryVersionsPortSample
 *
 * @see VersionsFileSource
 * @see RepositoryVersionSection
 */
interface RepositoryVersionsPort {
    /**
     * Reads the flat key/value view used by consumers that do not need section
     * grouping.
     */
    fun readVersions(
        source: VersionsFileSource,
    ): Map<String, String>

    /**
     * Reads the ordered section view used by `design-model.json` so Figma can
     * preserve the same version grouping seen in the source file.
     */
    fun readVersionSections(
        source: VersionsFileSource,
    ): List<RepositoryVersionSection>
}
