package com.marmatsan.figmaCatalogChecks.data.gradle

import com.marmatsan.figmaCatalogChecks.domain.model.*

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class GradleProjectModulesReaderTest : FunSpec({

    test("readModules returns root and build-logic included modules") {
        // GIVEN
        val rootSettingsFile = settingsFile(
            """
            include(
                ":app",
                ":core:core_ui"
            )
            """.trimIndent()
        )
        val buildLogicSettingsFile = settingsFile(
            """
            include(
                ":android",
                ":figmaCatalogChecks:data"
            )
            """.trimIndent()
        )

        // WHEN
        val modules = GradleProjectModulesReader().readModules(
            rootSettingsFile = rootSettingsFile,
            buildLogicSettingsFile = buildLogicSettingsFile
        )

        // THEN
        modules shouldBe setOf(
            ":app",
            ":build-logic:android",
            ":build-logic:figmaCatalogChecks:data",
            ":core:core_ui"
        )
    }
})

private fun settingsFile(content: String) =
    Files.createTempFile("settings", ".gradle.kts").toFile().apply {
        writeText(content)
    }
