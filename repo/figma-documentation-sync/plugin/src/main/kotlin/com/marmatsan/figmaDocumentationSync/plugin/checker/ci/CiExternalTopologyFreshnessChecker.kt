package com.marmatsan.figmaDocumentationSync.plugin.checker.ci

import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiExternalTopologyPort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiExternalTopologySource
import me.tatarka.inject.annotations.Inject
import java.io.File
import java.time.LocalDate

/**
 * Evaluates whether the manually verified external CI topology is still fresh.
 */
@Inject
internal class CiExternalTopologyFreshnessChecker(
    private val ciExternalTopologyPort: CiExternalTopologyPort,
) {
    /** Evaluates [topologyFile] against its declared warning interval at [currentDate]. */
    fun check(
        topologyFile: File,
        currentDate: LocalDate,
    ): Result {
        val topology =
            ciExternalTopologyPort.readTopology(
                source =
                    CiExternalTopologySource(
                        path = topologyFile.absolutePath,
                    ),
            )
        val warningDate =
            topology.validation.lastValidatedOn
                .plusDays(topology.validation.warnAfterDays.toLong())

        return Result(
            lastValidatedOn = topology.validation.lastValidatedOn,
            warningDate = warningDate,
            warningRequired = currentDate.isAfter(warningDate),
        )
    }

    /**
     * Freshness projection for a manually verified CI topology.
     *
     * @property lastValidatedOn date recorded by the topology contract.
     * @property warningDate first date after which revalidation is recommended.
     * @property warningRequired whether the evaluated date is past the warning date.
     */
    data class Result(
        val lastValidatedOn: LocalDate,
        val warningDate: LocalDate,
        val warningRequired: Boolean,
    )
}
