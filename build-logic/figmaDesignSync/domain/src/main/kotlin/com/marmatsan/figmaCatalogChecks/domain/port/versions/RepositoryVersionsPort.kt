package com.marmatsan.figmaDesignSync.domain.port.versions

import com.marmatsan.figmaDesignSync.domain.model.versions.RepositoryVersionSection

interface RepositoryVersionsPort {
    fun readVersions(source: VersionsFileSource): Map<String, String>

    fun readVersionSections(source: VersionsFileSource): List<RepositoryVersionSection>
}
