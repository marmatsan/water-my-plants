package com.marmatsan.figmaDesignSync.data.gradle.modules



import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

internal class GradleProjectModulesReaderTest : FunSpec({

    test("readModules returns root and included build modules") {
        // GIVEN
        val rootSettingsFile = settingsFile(
            """
            include(
                ":app",
                ":core:core_ui"
            )
            """.trimIndent()
        )
        val includedBuildSettingsFile = settingsFile(
            """
            include(
                ":android",
                ":catalog:data"
            )
            """.trimIndent()
        )
        includedBuildSettingsFile.parentFile
            .resolve("catalog")
            .mkdirs()

        // WHEN
        val modules = GradleProjectModulesReader().readModules(
            rootSettingsFile = rootSettingsFile,
            includedBuilds = listOf(
                GradleProjectModulesReader.IncludedBuild(
                    settingsFile = includedBuildSettingsFile,
                    modulePathPrefix = ":gradle-plugins"
                )
            )
        )

        // THEN
        modules shouldBe setOf(
            ":app",
            ":gradle-plugins:android",
            ":gradle-plugins:catalog",
            ":gradle-plugins:catalog:data",
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
