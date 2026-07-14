package com.marmatsan.figmaDesignSync.data.datasource.ci

import com.marmatsan.figmaDesignSync.data.teamcity.configuration.TeamCityGeneratedConfigurationReader
import com.marmatsan.figmaDesignSync.domain.model.ci.TeamCityConfiguration
import com.marmatsan.figmaDesignSync.domain.port.ci.TeamCityConfigurationPort
import com.marmatsan.figmaDesignSync.domain.port.ci.TeamCityGeneratedConfigurationSource
import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * Filesystem adapter for effective TeamCity generated configuration.
 */
@Inject
class TeamCityConfigurationDataSource(
    private val reader: TeamCityGeneratedConfigurationReader
) : TeamCityConfigurationPort {
    override fun readConfiguration(source: TeamCityGeneratedConfigurationSource): TeamCityConfiguration =
        reader.read(File(source.directoryPath))
}
