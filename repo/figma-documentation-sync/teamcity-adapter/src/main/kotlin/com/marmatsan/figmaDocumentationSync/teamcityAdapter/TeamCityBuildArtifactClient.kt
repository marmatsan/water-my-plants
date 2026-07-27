package com.marmatsan.figmaDocumentationSync.teamcityAdapter

import java.io.File

/** TeamCity operations needed to validate and download one build artifact set. */
interface TeamCityBuildArtifactClient {
    /** Reads the build identity that must be validated before artifact use. */
    fun readBuild(
        buildId: Long,
    ): TeamCityBuild

    /** Downloads the complete published artifact set for [buildId] into [outputDirectory]. */
    fun downloadArtifacts(
        buildId: Long,
        outputDirectory: File,
    )
}
