package com.marmatsan.figmaDesignSync.domain.port.versions

import com.marmatsan.figmaDesignSync.domain.model.versions.RepositoryVersionSection

/**
 * Port for reading repository version declarations from a version source.
 *
 * Implementations read `build-logic/versions.properties` and expose both a flat
 * key/value map and ordered [RepositoryVersionSection] values for Figma
 * documentation.
 *
 * @sample com.marmatsan.figmaDesignSync.domain.samples.DomainKDocSamples.repositoryVersionsPortSample
 */
interface RepositoryVersionsPort {
    fun readVersions(source: VersionsFileSource): Map<String, String>

    fun readVersionSections(source: VersionsFileSource): List<RepositoryVersionSection>
}
