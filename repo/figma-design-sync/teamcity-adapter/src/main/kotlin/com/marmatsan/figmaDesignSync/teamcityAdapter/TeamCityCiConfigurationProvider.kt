package com.marmatsan.figmaDesignSync.teamcityAdapter

import com.marmatsan.figmaDesignSync.data.ci.configuration.CiConfigurationProvider
import com.marmatsan.figmaDesignSync.domain.model.ci.CiConfiguration
import com.marmatsan.figmaDesignSync.teamcityAdapter.configuration.TeamCityGeneratedConfigurationReader
import java.io.File

/** Translates generated TeamCity Kotlin DSL output into the portable CI model. */
class TeamCityCiConfigurationProvider : CiConfigurationProvider {
    override fun read(directory: File): CiConfiguration =
        TeamCityGeneratedConfigurationReader().read(directory)
}
