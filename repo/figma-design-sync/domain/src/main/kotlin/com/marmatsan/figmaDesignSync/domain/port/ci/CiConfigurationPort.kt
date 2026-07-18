package com.marmatsan.figmaDesignSync.domain.port.ci

import com.marmatsan.figmaDesignSync.domain.model.ci.CiConfiguration

/** Port for reading the effective configuration selected by a project adapter. */
interface CiConfigurationPort {
    fun readConfiguration(source: CiGeneratedConfigurationSource): CiConfiguration
}
