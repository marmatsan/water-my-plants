package com.marmatsan.figmaCatalogChecks.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

internal class ModuleNamesComparisonTest : FunSpec({

    test("compare returns matching result when repository and Figma modules are equal") {
        // GIVEN
        val repositoryModules = setOf(":app", ":build-logic:android")
        val figmaModules = setOf(":build-logic:android", ":app")

        // WHEN
        val result = ModuleNamesComparison().compare(
            repositoryModules = repositoryModules,
            figmaModules = figmaModules
        )

        // THEN
        result.matches shouldBe true
    }

    test("compare reports missing and extra Figma modules") {
        // GIVEN
        val repositoryModules = setOf(":app", ":build-logic:android")
        val figmaModules = setOf(":app", ":build-logic:figmaVersions")

        // WHEN
        val result = ModuleNamesComparison().compare(
            repositoryModules = repositoryModules,
            figmaModules = figmaModules
        )

        // THEN
        result.matches shouldBe false
        result.missingInFigma shouldBe setOf(":build-logic:android")
        result.extraInFigma shouldBe setOf(":build-logic:figmaVersions")
        result.report() shouldContain "Missing in Figma"
        result.report() shouldContain "Extra in Figma"
    }
})
