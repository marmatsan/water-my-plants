package com.marmatsan.figmaDesignSync.data.datasource.versions


import com.marmatsan.figmaDesignSync.data.properties.versions.VersionsPropertiesReader
import com.marmatsan.figmaDesignSync.domain.model.versions.RepositoryVersionSection
import com.marmatsan.figmaDesignSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDesignSync.domain.port.versions.VersionsFileSource
import me.tatarka.inject.annotations.Inject
import java.io.File

@Inject
class RepositoryVersionsDataSource(
    private val versionsPropertiesReader: VersionsPropertiesReader
) : RepositoryVersionsPort {
    override fun readVersions(source: VersionsFileSource): Map<String, String> =
        versionsPropertiesReader.read(File(source.path))

    override fun readVersionSections(source: VersionsFileSource): List<RepositoryVersionSection> =
        versionsPropertiesReader.readSections(File(source.path))
}
