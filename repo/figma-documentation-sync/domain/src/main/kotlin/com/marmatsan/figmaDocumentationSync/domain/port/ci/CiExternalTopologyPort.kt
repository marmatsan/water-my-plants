package com.marmatsan.figmaDocumentationSync.domain.port.ci

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiExternalTopology

/**
 * Port for reading the repository-owned external CI topology.
 */
interface CiExternalTopologyPort {
    fun readTopology(
        source: CiExternalTopologySource
    ): CiExternalTopology
}
