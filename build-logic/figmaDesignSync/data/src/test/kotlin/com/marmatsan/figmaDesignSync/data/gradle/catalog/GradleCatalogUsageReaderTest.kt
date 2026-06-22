package com.marmatsan.figmaDesignSync.data.gradle.catalog

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

internal class GradleCatalogUsageReaderTest : FunSpec({

    test("readMainLiteralPluginUsages maps literal plugin ids to main modules") {
        // GIVEN
        val rootDir = Files.createTempDirectory("main-literal-plugin-usages").toFile()
        rootDir.writeBuildFile(
            path = "",
            content = """
            plugins {
                id("com.marmatsan.android") apply false
            }
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "app",
            content = """
            plugins {
                id("com.marmatsan.android")
                id("com.marmatsan.compose")
            }
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "core/ui",
            content = """
            plugins {
                id("com.marmatsan.android")
            }
            """.trimIndent()
        )

        // WHEN
        val usages = GradleCatalogUsageReader().readMainLiteralPluginUsages(rootDir)

        // THEN
        usages shouldBe mapOf(
            "com.marmatsan.android" to setOf(":app", ":core:ui"),
            "com.marmatsan.compose" to setOf(":app")
        )
    }
})

private fun File.writeBuildFile(
    path: String,
    content: String
) {
    val directory = resolve(path)
    directory.mkdirs()
    directory.resolve("build.gradle.kts").writeText(content)
}
