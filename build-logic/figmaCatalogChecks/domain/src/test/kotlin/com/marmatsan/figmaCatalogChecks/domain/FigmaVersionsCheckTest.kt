package com.marmatsan.figmaCatalogChecks.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class FigmaVersionsCheckTest : FunSpec({

    test("check returns match when URLs share file and versions are equal") {
        // GIVEN
        val check = FigmaVersionsCheck(VersionsComparison())
        val input = FigmaVersionsCheckInput(
            page = FigmaNodeReference(fileKey = "file", nodeId = "1:1"),
            section = FigmaNodeReference(fileKey = "file", nodeId = "2:1"),
            versionComponent = FigmaNodeReference(fileKey = "file", nodeId = "3:1"),
            repositoryVersions = mapOf("kotlinVersion" to "2.4.0"),
            figmaVersions = mapOf("kotlinVersion" to "2.4.0")
        )

        // WHEN
        val result = check.check(input)

        // THEN
        result shouldBe FigmaVersionsCheckResult.Match(
            sectionNodeId = "2:1",
            repositoryVersionCount = 1
        )
    }

    test("check reports different files before comparing versions") {
        // GIVEN
        val check = FigmaVersionsCheck(VersionsComparison())
        val input = FigmaVersionsCheckInput(
            page = FigmaNodeReference(fileKey = "file-a", nodeId = "1:1"),
            section = FigmaNodeReference(fileKey = "file-b", nodeId = "2:1"),
            versionComponent = FigmaNodeReference(fileKey = "file-c", nodeId = "3:1"),
            repositoryVersions = mapOf("kotlinVersion" to "2.4.0"),
            figmaVersions = mapOf("kotlinVersion" to "2.3.9")
        )

        // WHEN
        val result = check.check(input)

        // THEN
        result shouldBe FigmaVersionsCheckResult.DifferentFiles(
            fileKeys = listOf("file-a", "file-b", "file-c")
        )
    }

    test("check reports version mismatches") {
        // GIVEN
        val check = FigmaVersionsCheck(VersionsComparison())
        val input = FigmaVersionsCheckInput(
            page = FigmaNodeReference(fileKey = "file", nodeId = "1:1"),
            section = FigmaNodeReference(fileKey = "file", nodeId = "2:1"),
            versionComponent = FigmaNodeReference(fileKey = "file", nodeId = "3:1"),
            repositoryVersions = mapOf("kotlinVersion" to "2.4.0"),
            figmaVersions = mapOf("kotlinVersion" to "2.3.9")
        )

        // WHEN
        val result = check.check(input)

        // THEN
        result shouldBe FigmaVersionsCheckResult.VersionsMismatch(
            comparison = VersionsComparisonResult(
                missingInFigma = emptyMap(),
                extraInFigma = emptyMap(),
                changedValues = mapOf(
                    "kotlinVersion" to VersionDifference(
                        repositoryValue = "2.4.0",
                        figmaValue = "2.3.9"
                    )
                )
            )
        )
    }
})
