package com.marmatsan.figmaDocumentationSync.plugin.checker.ci

import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiExternalTopologyPort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiExternalTopologySource
import java.io.File
import java.time.LocalDate
import me.tatarka.inject.annotations.Inject

/**
 * Evaluates whether the manually verified external CI topology is still fresh.
 */
@Inject
internal class CiExternalTopologyFreshnessChecker(
    private val ciExternalTopologyPort: CiExternalTopologyPort
) {
    fun check(
        topologyFile: File,
        currentDate: LocalDate
    ): Result {
        val topology = ciExternalTopologyPort.readTopology(
            CiExternalTopologySource(topologyFile.absolutePath)
        )
        val warningDate = topology.validation.lastValidatedOn
            .plusDays(topology.validation.warnAfterDays.toLong())

        return Result(
            lastValidatedOn = topology.validation.lastValidatedOn,
            warningDate = warningDate,
            warningRequired = currentDate.isAfter(warningDate)
        )
    }

    data class Result(
        val lastValidatedOn: LocalDate,
        val warningDate: LocalDate,
        val warningRequired: Boolean
    )
}
