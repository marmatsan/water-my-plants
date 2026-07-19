package com.marmatsan.figmaDocumentationSync.teamcityAdapter

import com.marmatsan.figmaDocumentationSync.data.ci.configuration.CiConfigurationProvider
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConfiguration
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.configuration.TeamCityGeneratedConfigurationReader
import java.io.File

/** Translates generated TeamCity Kotlin DSL output into the portable CI model. */
class TeamCityCiConfigurationProvider : CiConfigurationProvider {
    override fun read(directory: File): CiConfiguration =
        TeamCityGeneratedConfigurationReader().read(directory)
}
