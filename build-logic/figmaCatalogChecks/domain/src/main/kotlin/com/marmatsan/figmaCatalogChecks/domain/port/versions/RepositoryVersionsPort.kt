package com.marmatsan.figmaCatalogChecks.domain.port.versions

interface RepositoryVersionsPort {
    fun readVersions(source: VersionsFileSource): Map<String, String>
}
