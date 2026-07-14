package com.marmatsan.figmaDesignSync.domain.port.ci

import com.marmatsan.figmaDesignSync.domain.model.ci.CiExternalTopology

/**
 * Port for reading the repository-owned external CI topology.
 */
interface CiExternalTopologyPort {
    fun readTopology(source: CiExternalTopologySource): CiExternalTopology
}
