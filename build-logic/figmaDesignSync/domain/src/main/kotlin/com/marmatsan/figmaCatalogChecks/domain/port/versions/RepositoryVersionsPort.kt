package com.marmatsan.figmaDesignSync.domain.port.versions

interface RepositoryVersionsPort {
    fun readVersions(source: VersionsFileSource): Map<String, String>
}
