package com.marmatsan.figmaDocumentationSync.data.datasource.ci

import com.marmatsan.figmaDocumentationSync.data.yaml.ci.CiExternalTopologyYamlReader
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiExternalTopologyPort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiExternalTopologySource
import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * Filesystem adapter for the versioned external CI topology.
 */
@Inject
class CiExternalTopologyDataSource(
    private val reader: CiExternalTopologyYamlReader,
) : CiExternalTopologyPort {
    /** Reads the repository-owned external topology YAML selected by [source]. */
    override fun readTopology(
        source: CiExternalTopologySource,
    ): CiExternalTopology =
        reader.read(File(source.path))
}
