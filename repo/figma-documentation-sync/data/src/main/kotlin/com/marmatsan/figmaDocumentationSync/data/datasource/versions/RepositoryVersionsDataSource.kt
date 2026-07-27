package com.marmatsan.figmaDocumentationSync.data.datasource.versions

import com.marmatsan.figmaDocumentationSync.data.properties.versions.VersionsPropertiesReader
import com.marmatsan.figmaDocumentationSync.domain.model.versions.RepositoryVersionSection
import com.marmatsan.figmaDocumentationSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDocumentationSync.domain.port.versions.VersionsFileSource
import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * Adapter that reads repository version declarations from
 * the versions file selected by the consuming project.
 *
 * It preserves both the flat key/value view and the sectioned view so the
 * generator can keep the Figma artifact aligned with the source file.
 */
@Inject
class RepositoryVersionsDataSource(
    private val versionsPropertiesReader: VersionsPropertiesReader,
) : RepositoryVersionsPort {
    /** Reads the flattened version values from [source]. */
    override fun readVersions(
        source: VersionsFileSource,
    ): Map<String, String> =
        versionsPropertiesReader.read(File(source.path))

    /** Reads version values grouped by their documented properties sections. */
    override fun readVersionSections(
        source: VersionsFileSource,
    ): List<RepositoryVersionSection> =
        versionsPropertiesReader.readSections(
            file = File(source.path),
        )
}
