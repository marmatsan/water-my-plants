package com.marmatsan.figmaDesignSync.data.datasource.ci

import com.marmatsan.figmaDesignSync.data.yaml.ci.CiExternalTopologyYamlReader
import com.marmatsan.figmaDesignSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDesignSync.domain.port.ci.CiExternalTopologyPort
import com.marmatsan.figmaDesignSync.domain.port.ci.CiExternalTopologySource
import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * Filesystem adapter for the versioned external CI topology.
 */
@Inject
class CiExternalTopologyDataSource(
    private val reader: CiExternalTopologyYamlReader
) : CiExternalTopologyPort {
    override fun readTopology(source: CiExternalTopologySource): CiExternalTopology =
        reader.read(File(source.path))
}
