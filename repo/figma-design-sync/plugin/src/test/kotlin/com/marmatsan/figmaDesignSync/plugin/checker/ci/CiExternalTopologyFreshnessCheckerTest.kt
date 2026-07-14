package com.marmatsan.figmaDesignSync.plugin.checker.ci

import com.marmatsan.figmaDesignSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDesignSync.domain.port.ci.CiExternalTopologyPort
import com.marmatsan.figmaDesignSync.domain.port.ci.CiExternalTopologySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

internal class CiExternalTopologyFreshnessCheckerTest : FunSpec({

    test("check requests a warning only after the configured validation window") {
        val checker = CiExternalTopologyFreshnessChecker(FakeCiExternalTopologyPort)

        checker.check(
            topologyFile = java.io.File("external-topology.yaml"),
            currentDate = LocalDate.parse("2026-10-12")
        ).warningRequired shouldBe false

        checker.check(
            topologyFile = java.io.File("external-topology.yaml"),
            currentDate = LocalDate.parse("2026-10-13")
        ).warningRequired shouldBe true
    }
})

private object FakeCiExternalTopologyPort : CiExternalTopologyPort {
    override fun readTopology(source: CiExternalTopologySource): CiExternalTopology =
        CiExternalTopology(
            schemaVersion = 1,
            validation = CiExternalTopology.Validation(
                lastValidatedOn = LocalDate.parse("2026-07-14"),
                warnAfterDays = 90
            ),
            nodes = emptyList(),
            connections = emptyList()
        )
}
