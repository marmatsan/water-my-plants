package com.marmatsan.figmaCatalogChecks.data.datasource.versions


import com.marmatsan.figmaCatalogChecks.data.properties.versions.VersionsPropertiesReader
import com.marmatsan.figmaCatalogChecks.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaCatalogChecks.domain.port.versions.VersionsFileSource
import me.tatarka.inject.annotations.Inject
import java.io.File

@Inject
class RepositoryVersionsDataSource(
    private val versionsPropertiesReader: VersionsPropertiesReader
) : RepositoryVersionsPort {
    override fun readVersions(source: VersionsFileSource): Map<String, String> =
        versionsPropertiesReader.read(File(source.path))
}
