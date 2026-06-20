package com.marmatsan.figmaDesignSync.data.gradle.modules



import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
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
                ":figmaDesignSync:data"
            )
            """.trimIndent()
        )
        buildLogicSettingsFile.parentFile
            .resolve("figmaDesignSync")
            .mkdirs()

        // WHEN
        val modules = GradleProjectModulesReader().readModules(
            rootSettingsFile = rootSettingsFile,
            buildLogicSettingsFile = buildLogicSettingsFile
        )

        // THEN
        modules shouldBe setOf(
            ":app",
            ":build-logic:android",
            ":build-logic:figmaDesignSync",
            ":build-logic:figmaDesignSync:data",
            ":core:core_ui"
        )
    }
})

private fun settingsFile(content: String): File =
    Files.createTempDirectory("gradle-project-modules-reader")
        .resolve("settings.gradle.kts")
        .toFile()
        .apply {
        writeText(content)
    }
