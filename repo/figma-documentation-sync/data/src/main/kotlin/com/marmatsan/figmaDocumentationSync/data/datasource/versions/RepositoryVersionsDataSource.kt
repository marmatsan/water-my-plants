package com.marmatsan.figmaDocumentationSync.data.datasource.versions


import com.marmatsan.figmaDocumentationSync.data.properties.versions.VersionsPropertiesReader
import com.marmatsan.figmaDocumentationSync.domain.model.versions.RepositoryVersionSection
import com.marmatsan.figmaDocumentationSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDocumentationSync.domain.port.versions.VersionsFileSource
import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * Adapter that reads repository version declarations from
 * `repo/dependency-catalog/versions.properties`.
 *
 * It preserves both the flat key/value view and the sectioned view so the
 * generator can keep the Figma artifact aligned with the source file.
 */
@Inject
class RepositoryVersionsDataSource(
    private val versionsPropertiesReader: VersionsPropertiesReader
) : RepositoryVersionsPort {
    override fun readVersions(
        source: VersionsFileSource
    ): Map<String, String> =
        versionsPropertiesReader.read(File(source.path))

    override fun readVersionSections(
        source: VersionsFileSource
    ): List<RepositoryVersionSection> =
        versionsPropertiesReader.readSections(File(source.path))
}
