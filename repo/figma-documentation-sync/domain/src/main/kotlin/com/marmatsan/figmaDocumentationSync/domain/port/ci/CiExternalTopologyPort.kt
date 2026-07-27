package com.marmatsan.figmaDocumentationSync.domain.port.ci

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiExternalTopology

/**
 * Port for reading the repository-owned external CI topology.
 */
interface CiExternalTopologyPort {
    /** Reads and validates the versioned external topology at [source]. */
    fun readTopology(
        source: CiExternalTopologySource,
    ): CiExternalTopology
}
