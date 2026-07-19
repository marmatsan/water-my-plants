package com.marmatsan.figmaDesignSync.teamcityAdapter

/** Mutating boundary for queueing one TeamCity run. */
fun interface TeamCityRunStarter {
    fun startRun(buildTypeId: String, branch: String): TeamCityRun
}
