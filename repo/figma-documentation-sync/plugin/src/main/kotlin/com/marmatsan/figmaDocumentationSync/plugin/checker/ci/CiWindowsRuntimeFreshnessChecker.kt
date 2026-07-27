package com.marmatsan.figmaDocumentationSync.plugin.checker.ci

import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiWindowsRuntimePort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiWindowsRuntimeSource
import me.tatarka.inject.annotations.Inject
import java.io.File
import java.time.LocalDate

/**
 * Evaluates whether the manually verified Windows CI runtime is still fresh.
 */
@Inject
internal class CiWindowsRuntimeFreshnessChecker(
    private val ciWindowsRuntimePort: CiWindowsRuntimePort,
) {
    /** Evaluates [runtimeFile] against its declared warning interval at [currentDate]. */
    fun check(
        runtimeFile: File,
        currentDate: LocalDate,
    ): Result {
        val runtime =
            ciWindowsRuntimePort.readRuntime(
                source =
                    CiWindowsRuntimeSource(
                        filePath = runtimeFile.absolutePath,
                    ),
            )
        val warningDate =
            runtime.validation.lastValidatedOn
                .plusDays(runtime.validation.warnAfterDays.toLong())

        return Result(
            lastValidatedOn = runtime.validation.lastValidatedOn,
            warningDate = warningDate,
            warningRequired = currentDate.isAfter(warningDate),
        )
    }

    /**
     * Freshness projection for the manually verified Windows CI runtime.
     *
     * @property lastValidatedOn date recorded by the runtime contract.
     * @property warningDate first date after which revalidation is recommended.
     * @property warningRequired whether the evaluated date is past the warning date.
     */
    data class Result(
        val lastValidatedOn: LocalDate,
        val warningDate: LocalDate,
        val warningRequired: Boolean,
    )
}
