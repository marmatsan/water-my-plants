package com.marmatsan.figmaDesignSync.domain.port.ci

import com.marmatsan.figmaDesignSync.domain.model.ci.TeamCityConfiguration

/**
 * Port for reading effective TeamCity generated configuration.
 */
interface TeamCityConfigurationPort {
    fun readConfiguration(source: TeamCityGeneratedConfigurationSource): TeamCityConfiguration
}
