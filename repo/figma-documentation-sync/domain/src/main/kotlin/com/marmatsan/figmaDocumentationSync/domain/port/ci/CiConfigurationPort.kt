package com.marmatsan.figmaDocumentationSync.domain.port.ci

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConfiguration

/** Port for reading the effective configuration selected by a project adapter. */
interface CiConfigurationPort {
    /** Reads the effective CI configuration from the adapter selected by [source]. */
    fun readConfiguration(
        source: CiGeneratedConfigurationSource,
    ): CiConfiguration
}
