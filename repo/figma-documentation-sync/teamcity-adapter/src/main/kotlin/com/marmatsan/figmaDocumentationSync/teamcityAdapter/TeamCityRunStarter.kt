package com.marmatsan.figmaDocumentationSync.teamcityAdapter

/** Mutating boundary for queueing one TeamCity run. */
fun interface TeamCityRunStarter {
    /** Queues [buildTypeId] on [branch] and returns the created run identity. */
    fun startRun(
        buildTypeId: String,
        branch: String,
    ): TeamCityRun
}
