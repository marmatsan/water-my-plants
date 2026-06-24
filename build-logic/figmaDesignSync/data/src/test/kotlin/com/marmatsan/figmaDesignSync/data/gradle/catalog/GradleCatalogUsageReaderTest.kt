package com.marmatsan.figmaDesignSync.data.gradle.catalog

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

internal class GradleCatalogUsageReaderTest : FunSpec({

    test("readConventionLibraryUsages maps wrapper catalog dependencies to convention modules") {
        // GIVEN
        val rootDir = Files.createTempDirectory("convention-library-usages").toFile()
        rootDir.writeKotlinFile(
            path = "build-logic/android/src/main/kotlin/com/marmatsan/android/plugin",
            fileName = "AndroidGradleConventionPlugin.kt",
            content = """
            package com.marmatsan.android.plugin

            fun configure() {
                val libs = withVersionCatalog(libs)
                libs.implementation(
                    libraryGroup = "androidx.core",
                    artifact = "core-ktx"
                )
                libs.implementationPlatform(
                    libraryGroup = "androidx.compose",
                    artifact = "compose-bom"
                )
                libs.implementationBundle(
                    bundle = "composeBundle"
                )
            }
            """.trimIndent()
        )

        // WHEN
        val usages = GradleCatalogUsageReader().readConventionLibraryUsages(rootDir)

        // THEN
        usages shouldBe GradleCatalogUsageReader.LibraryUsages(
            coordinates = mapOf(
                "androidx.core:core-ktx" to setOf(":build-logic:android"),
                "androidx.compose:compose-bom" to setOf(":build-logic:android")
            ),
            bundles = mapOf(
                "composeBundle" to setOf(":build-logic:android")
            )
        )
    }

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

    test("readMainAppliedLiteralPluginUsages maps applied literal plugin ids to main modules") {
        // GIVEN
        val rootDir = Files.createTempDirectory("main-applied-literal-plugin-usages").toFile()
        rootDir.writeBuildFile(
            path = "",
            content = """
            plugins {
                id("com.marmatsan.android") apply false
                id("com.marmatsan.figmaDesignSync") apply true
            }
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "app",
            content = """
            plugins {
                id("com.marmatsan.android")
            }
            """.trimIndent()
        )

        // WHEN
        val usages = GradleCatalogUsageReader().readMainAppliedLiteralPluginUsages(rootDir)

        // THEN
        usages shouldBe mapOf(
            "com.marmatsan.android" to setOf(":app")
        )
    }

    test("readMainAppliedLiteralPluginIds includes applied root plugin ids") {
        // GIVEN
        val rootDir = Files.createTempDirectory("main-applied-literal-plugin-ids").toFile()
        rootDir.writeBuildFile(
            path = "",
            content = """
            plugins {
                id("com.marmatsan.android") apply false
                id("com.marmatsan.figmaDesignSync") apply true
            }
            """.trimIndent()
        )

        // WHEN
        val pluginIds = GradleCatalogUsageReader().readMainAppliedLiteralPluginIds(rootDir)

        // THEN
        pluginIds shouldBe setOf("com.marmatsan.figmaDesignSync")
    }
})

private fun File.writeKotlinFile(
    path: String,
    fileName: String,
    content: String
) {
    val directory = resolve(path)
    directory.mkdirs()
    directory.resolve(fileName).writeText(content)
}

private fun File.writeBuildFile(
    path: String,
    content: String
) {
    val directory = resolve(path)
    directory.mkdirs()
    directory.resolve("build.gradle.kts").writeText(content)
}
