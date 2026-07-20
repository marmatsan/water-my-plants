package com.marmatsan.figmaDocumentationSync.domain.port.ci

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConfiguration

/** Port for reading the effective configuration selected by a project adapter. */
interface CiConfigurationPort {
    fun readConfiguration(
        source: CiGeneratedConfigurationSource
    ): CiConfiguration
}
