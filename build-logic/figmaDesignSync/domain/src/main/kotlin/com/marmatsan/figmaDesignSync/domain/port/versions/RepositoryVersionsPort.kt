package com.marmatsan.figmaDesignSync.domain.port.versions

import com.marmatsan.figmaDesignSync.domain.model.versions.RepositoryVersionSection

/**
 * Port for reading repository version declarations from a version source.
 *
 * Implementations read `build-logic/versions.properties` and expose both a flat
 * key/value map and ordered [RepositoryVersionSection] values for Figma
 * documentation.
 *
 * Example:
 * ```
 * val source = VersionsFileSource("build-logic/versions.properties")
 * val versions = port.readVersions(source)
 * val sections = port.readVersionSections(source)
 * ```
 */
interface RepositoryVersionsPort {
    fun readVersions(source: VersionsFileSource): Map<String, String>

    fun readVersionSections(source: VersionsFileSource): List<RepositoryVersionSection>
}
