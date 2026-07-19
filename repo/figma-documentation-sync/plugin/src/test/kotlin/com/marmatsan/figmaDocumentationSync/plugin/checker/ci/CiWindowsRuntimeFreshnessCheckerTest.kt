package com.marmatsan.figmaDocumentationSync.plugin.checker.ci

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiWindowsRuntime
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiWindowsRuntimePort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiWindowsRuntimeSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

internal class CiWindowsRuntimeFreshnessCheckerTest : FunSpec({

    test("check requests a warning only after the configured validation window") {
        val checker = CiWindowsRuntimeFreshnessChecker(FakeCiWindowsRuntimePort)

        checker.check(
            runtimeFile = java.io.File("windows-runtime.yaml"),
            currentDate = LocalDate.parse("2026-10-14")
        ).warningRequired shouldBe false

        checker.check(
            runtimeFile = java.io.File("windows-runtime.yaml"),
            currentDate = LocalDate.parse("2026-10-15")
        ).warningRequired shouldBe true
    }
})

private object FakeCiWindowsRuntimePort : CiWindowsRuntimePort {
    override fun readRuntime(source: CiWindowsRuntimeSource): CiWindowsRuntime =
        CiWindowsRuntime(
            schemaVersion = 1,
            validation = CiWindowsRuntime.Validation(
                lastValidatedOn = LocalDate.parse("2026-07-16"),
                warnAfterDays = 90
            ),
            platform = "Windows",
            services = listOf(
                CiWindowsRuntime.Service(
                    id = "teamcity-server",
                    name = "TeamCity Server",
                    description = "Hosts TeamCity.",
                    service = "TeamCity",
                    startup = "Automatic",
                    identity = "LocalSystem"
                )
            )
        )
}
