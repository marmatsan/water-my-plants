package com.marmatsan.figmaVersions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

internal class VersionsComparisonTest : FunSpec({

    test("compare returns matching result when repository and Figma versions are equal") {
        // GIVEN
        val repositoryVersions = mapOf(
            "kotlinVersion" to "2.4.0",
            "composeBomVersion" to "2026.05.01"
        )
        val figmaVersions = mapOf(
            "composeBomVersion" to "2026.05.01",
            "kotlinVersion" to "2.4.0"
        )

        // WHEN
        val result = VersionsComparison().compare(
            repositoryVersions = repositoryVersions,
            figmaVersions = figmaVersions
        )

        // THEN
        result.matches shouldBe true
    }

    test("compare reports versions missing in Figma") {
        // GIVEN
        val repositoryVersions = mapOf("kotlinVersion" to "2.4.0")
        val figmaVersions = emptyMap<String, String>()

        // WHEN
        val result = VersionsComparison().compare(
            repositoryVersions = repositoryVersions,
            figmaVersions = figmaVersions
        )

        // THEN
        result.matches shouldBe false
        result.missingInFigma shouldBe mapOf("kotlinVersion" to "2.4.0")
        result.report() shouldContain "Missing in Figma"
    }

    test("compare reports extra versions in Figma") {
        // GIVEN
        val repositoryVersions = emptyMap<String, String>()
        val figmaVersions = mapOf("kotlinVersion" to "2.4.0")

        // WHEN
        val result = VersionsComparison().compare(
            repositoryVersions = repositoryVersions,
            figmaVersions = figmaVersions
        )

        // THEN
        result.matches shouldBe false
        result.extraInFigma shouldBe mapOf("kotlinVersion" to "2.4.0")
        result.report() shouldContain "Extra in Figma"
    }

    test("compare reports changed values") {
        // GIVEN
        val repositoryVersions = mapOf("kotlinVersion" to "2.4.0")
        val figmaVersions = mapOf("kotlinVersion" to "2.3.9")

        // WHEN
        val result = VersionsComparison().compare(
            repositoryVersions = repositoryVersions,
            figmaVersions = figmaVersions
        )

        // THEN
        result.matches shouldBe false
        result.changedValues shouldBe mapOf(
            "kotlinVersion" to VersionDifference(
                repositoryValue = "2.4.0",
                figmaValue = "2.3.9"
            )
        )
        result.report() shouldContain "Changed values"
    }
})
