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
    private val ciWindowsRuntimePort: CiWindowsRuntimePort
) {
    fun check(
        runtimeFile: File,
        currentDate: LocalDate
    ): Result {
        val runtime = ciWindowsRuntimePort.readRuntime(
            CiWindowsRuntimeSource(runtimeFile.absolutePath)
        )
        val warningDate = runtime.validation.lastValidatedOn
            .plusDays(runtime.validation.warnAfterDays.toLong())

        return Result(
            lastValidatedOn = runtime.validation.lastValidatedOn,
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
