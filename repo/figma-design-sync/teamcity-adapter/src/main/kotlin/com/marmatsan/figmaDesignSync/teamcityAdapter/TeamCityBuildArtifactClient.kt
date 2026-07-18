package com.marmatsan.figmaDesignSync.teamcityAdapter

import java.io.File

/** TeamCity operations needed to validate and download one build artifact set. */
interface TeamCityBuildArtifactClient {
    fun readBuild(buildId: Long): TeamCityBuild

    fun downloadArtifacts(buildId: Long, outputDirectory: File)
}
