package com.marmatsan.figmaDesignSync.data.dependencies.catalog

import com.marmatsan.dependencies.tree.dsl.library.libraryTree
import com.marmatsan.dependencies.tree.dsl.plugin.pluginTree
import com.marmatsan.figmaDesignSync.data.gradle.catalog.GradleCatalogUsageReader
import com.marmatsan.figmaDesignSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginConfigurationUsage
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginUsage
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDesignSync.domain.port.gradle.IncludedBuildSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

internal class DependenciesCatalogTreesReaderTest : FunSpec({

    test("readLibraryTree maps dependency library trees to catalog library trees") {
        // GIVEN
        val dependencyTree = libraryTree("androidx") {
            library("compose") {
                artifact(
                    artifact = "compose-bom",
                    version = "2026.05.01"
                )
                library("ui") {
                    artifactsBundle(
                        "ui",
                        "ui-tooling",
                        alias = "composeBundle"
                    )
                }
            }
        }

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readLibraryTree(listOf(dependencyTree))

        // THEN
        actualTree shouldBe LibraryCatalogTree(
            roots = listOf(
                LibraryCatalogNode(
                    group = "androidx",
                    children = listOf(
                        LibraryCatalogNode(
                            group = "compose",
                            entries = listOf(
                                LibraryCatalogEntry.Artifact(
                                    artifact = "compose-bom",
                                    version = CatalogVersion("2026.05.01")
                                )
                            ),
                            children = listOf(
                                LibraryCatalogNode(
                                    group = "ui",
                                    entries = listOf(
                                        LibraryCatalogEntry.ArtifactsBundle(
                                            alias = "composeBundle",
                                            artifacts = listOf(
                                                "ui",
                                                "ui-tooling"
                                            ),
                                            version = CatalogVersion(null)
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    test("readPluginTree maps dependency plugin trees to catalog plugin trees") {
        // GIVEN
        val dependencyTree = pluginTree("org") {
            plugin("jetbrains") {
                plugin("kotlin") {
                    plugin(
                        id = "android",
                        version = "2.4.0"
                    )
                }
            }
        }

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readPluginTree(listOf(dependencyTree))

        // THEN
        actualTree shouldBe PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(
                    id = "org",
                    children = listOf(
                        PluginCatalogNode(
                            id = "jetbrains",
                            children = listOf(
                                PluginCatalogNode(
                                    id = "kotlin",
                                    children = listOf(
                                        PluginCatalogNode(
                                            id = "android",
                                            version = CatalogVersion("2.4.0")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    test("readLibraryTreeWithVersionAliases maps versions as property aliases") {
        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readLibraryTreeWithVersionAliases(rootDir = java.io.File("."))

        // THEN
        val activity = actualTree.roots
            .first { node -> node.group == "androidx" }
            .children
            .first { node -> node.group == "activity" }
            .entries
            .single() as LibraryCatalogEntry.Artifact

        activity.version shouldBe CatalogVersion("activityComposeLibraryVersion")
    }

    test("readPluginTreeWithVersionAliases maps plugin versions as property aliases") {
        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readPluginTreeWithVersionAliases(rootDir = java.io.File("."))

        // THEN
        actualTree.findPlugin("com.google.devtools.ksp").version shouldBe CatalogVersion("kspPluginVersion")
        actualTree.findPlugin("org.jetbrains.dokka").version shouldBe CatalogVersion("dokkaPluginVersion")
        actualTree.findPlugin("org.jetbrains.kotlin.plugin.compose").version shouldBe CatalogVersion("kotlinVersion")
    }

    test("readLibraryTreeWithVersionAliases scopes required modules to the main build catalog") {
        // GIVEN
        val rootDir = Files.createTempDirectory("water-my-plants-library-usages").toFile()
        rootDir.writeBuildFile(
            path = "app",
            content = """
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(platform(libs.androidx.compose.bom))
                implementation(libs.bundles.composeBundle)
            }
            """.trimIndent()
        )
        rootDir.writeSettingsFile(
            path = "repo/gradle-plugins",
            content = """
            rootProject.name = "gradle-plugins"
            include(":unit-test")
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "repo/gradle-plugins/unit-test",
            content = """
            dependencies {
                testImplementation(libs.io.mockk)
            }
            """.trimIndent()
        )

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readLibraryTreeWithVersionAliases(rootDir = rootDir)

        // THEN
        actualTree.findArtifact("androidx.core", "core-ktx").requiredByModules shouldBe listOf(":app")
        actualTree.findArtifact("androidx.compose", "compose-bom").requiredByModules shouldBe listOf(":app")
        actualTree.findBundle("androidx.compose.ui", "composeBundle").requiredByModules shouldBe listOf(":app")
        actualTree.findArtifact("io.mockk", "mockk").requiredByModules shouldBe emptyList()
    }

    test("readLibraryTreeWithVersionAliases maps convention plugin providers to main build modules") {
        // GIVEN
        val rootDir = Files.createTempDirectory("water-my-plants-convention-library-usages").toFile()
        rootDir.writeBuildFile(
            path = "app",
            content = """
            plugins {
                id("com.marmatsan.compose")
            }
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "core/ui",
            content = """
            plugins {
                id("com.marmatsan.compose")
            }
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "onboarding/ui",
            content = """
            plugins {
                id("com.marmatsan.android")
            }
            """.trimIndent()
        )
        val includedBuildRootDir = rootDir.resolve("repo/gradle-plugins")
        includedBuildRootDir.writeBuildFile(
            path = "compose",
            content = """
            gradlePlugin {
                val pluginName = "com.marmatsan.compose"
                plugins.register(pluginName) {
                    id = pluginName
                    implementationClass = "com.marmatsan.compose.plugin.ComposeGradleConventionPlugin"
                }
            }
            """.trimIndent()
        )
        includedBuildRootDir.writeKotlinFile(
            path = "compose/src/main/kotlin/com/marmatsan/compose/plugin",
            fileName = "ComposeGradleConventionPlugin.kt",
            content = """
            package com.marmatsan.compose.plugin

            fun configure() {
                libs.implementation(
                    libraryGroup = "androidx.activity",
                    artifact = "activity-compose"
                )
                libs.implementationBundle(
                    bundle = "composeBundle"
                )
            }
            """.trimIndent()
        )
        includedBuildRootDir.writeBuildFile(
            path = "unit-test",
            content = """
            gradlePlugin {
                val pluginName = "com.marmatsan.unitTest"
                plugins.register(pluginName) {
                    id = pluginName
                    implementationClass = "com.marmatsan.unitTest.plugin.UnitTestGradleConventionPlugin"
                }
            }
            """.trimIndent()
        )
        includedBuildRootDir.writeKotlinFile(
            path = "unit-test/src/main/kotlin/com/marmatsan/unitTest/plugin",
            fileName = "UnitTestGradleConventionPlugin.kt",
            content = """
            package com.marmatsan.unitTest.plugin

            fun configure() {
                libs.implementation(
                    libraryGroup = "io.mockk",
                    artifact = "mockk"
                )
            }
            """.trimIndent()
        )

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readLibraryTreeWithVersionAliases(
            rootDir = rootDir,
            conventionPluginIncludedBuilds = listOf(
                IncludedBuildSource(
                    settingsFilePath = includedBuildRootDir.resolve("settings.gradle.kts").absolutePath,
                    rootDirPath = includedBuildRootDir.absolutePath,
                    modulePathPrefix = ":gradle-plugins",
                    publishesConventionPlugins = true
                )
            )
        )

        // THEN
        val expectedComposeUsage = listOf(
            ConventionPluginUsage(
                pluginId = "com.marmatsan.compose",
                pluginModule = ":gradle-plugins:compose",
                requiredByModules = listOf(":app", ":core:ui")
            )
        )
        val expectedUnusedUnitTestUsage = listOf(
            ConventionPluginUsage(
                pluginId = "com.marmatsan.unitTest",
                pluginModule = ":gradle-plugins:unit-test",
                requiredByModules = emptyList()
            )
        )
        actualTree.findArtifact("androidx.activity", "activity-compose")
            .providedByConventionPlugins shouldBe expectedComposeUsage
        actualTree.findBundle("androidx.compose.ui", "composeBundle")
            .providedByConventionPlugins shouldBe expectedComposeUsage
        actualTree.findArtifact("io.mockk", "mockk")
            .providedByConventionPlugins shouldBe expectedUnusedUnitTestUsage
    }

    test("readLibraryTreeWithVersionAliases maps convention plugin tool artifact configuration") {
        // GIVEN
        val rootDir = Files.createTempDirectory("water-my-plants-convention-library-configuration").toFile()
        val includedBuildRootDir = rootDir.resolve("repo/gradle-plugins")
        includedBuildRootDir.writeBuildFile(
            path = "protobuf",
            content = """
            gradlePlugin {
                val pluginName = "com.marmatsan.protobuf"
                plugins.register(pluginName) {
                    id = pluginName
                    implementationClass = "com.marmatsan.protobuf.plugin.ProtobufGradleConventionPlugin"
                }
            }
            """.trimIndent()
        )
        includedBuildRootDir.writeKotlinFile(
            path = "protobuf/src/main/kotlin/com/marmatsan/protobuf/plugin",
            fileName = "ProtobufGradleConventionPlugin.kt",
            content = """
            package com.marmatsan.protobuf.plugin

            fun configure() {
                libs.implementation(
                    libraryGroup = "com.google.protobuf",
                    artifact = "protobuf-kotlin"
                )
                project.extensions.configure<ProtobufExtension>("protobuf") {
                    protoc {
                        artifact = libs.requireDependencyNotation(
                            libraryGroup = "com.google.protobuf",
                            artifact = "protoc"
                        )
                    }
                }
            }
            """.trimIndent()
        )

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readLibraryTreeWithVersionAliases(
            rootDir = rootDir,
            conventionPluginIncludedBuilds = listOf(
                IncludedBuildSource(
                    settingsFilePath = includedBuildRootDir.resolve("settings.gradle.kts").absolutePath,
                    rootDirPath = includedBuildRootDir.absolutePath,
                    modulePathPrefix = ":gradle-plugins",
                    publishesConventionPlugins = true
                )
            )
        )

        // THEN
        val protobufKotlin = actualTree.findArtifact("com.google.protobuf", "protobuf-kotlin")
        protobufKotlin.requiredByModules shouldBe emptyList()
        protobufKotlin.providedByConventionPlugins shouldBe listOf(
            ConventionPluginUsage(
                pluginId = "com.marmatsan.protobuf",
                pluginModule = ":gradle-plugins:protobuf",
                requiredByModules = emptyList()
            )
        )
        protobufKotlin.configuredByConventionPlugins shouldBe emptyList()

        val protoc = actualTree.findArtifact("com.google.protobuf", "protoc")
        protoc.requiredByModules shouldBe emptyList()
        protoc.providedByConventionPlugins shouldBe emptyList()
        protoc.configuredByConventionPlugins shouldBe listOf(
            ConventionPluginConfigurationUsage(
                pluginId = "com.marmatsan.protobuf",
                pluginModule = ":gradle-plugins:protobuf",
                target = "protobuf.protoc.artifact"
            )
        )
    }

    test("readPluginTreeWithVersionAliases scopes applied modules to the main build catalog") {
        // GIVEN
        val rootDir = Files.createTempDirectory("water-my-plants-plugin-usages").toFile()
        rootDir.writeBuildFile(
            path = "app",
            content = """
            plugins {
                alias(plugins.plugins.com.android.application)
            }
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "core/ui",
            content = """
            plugins {
                alias(plugins.plugins.com.android.library)
            }
            """.trimIndent()
        )
        rootDir.writeSettingsFile(
            path = "repo/gradle-plugins",
            content = """
            rootProject.name = "gradle-plugins"
            include(":dokka-documentation")
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "repo/gradle-plugins/dokka-documentation",
            content = """
            plugins {
                alias(plugins.plugins.org.jetbrains.dokka)
            }
            """.trimIndent()
        )

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readPluginTreeWithVersionAliases(rootDir = rootDir)

        // THEN
        actualTree.findPlugin("com.android.application").appliedToModules shouldBe listOf(":app")
        actualTree.findPlugin("com.android.library").appliedToModules shouldBe listOf(":core:ui")
        actualTree.findPlugin("org.jetbrains.dokka").appliedToModules shouldBe emptyList()
    }

    test("readPluginTreeWithVersionAliases maps convention plugin providers to main build modules") {
        // GIVEN
        val rootDir = Files.createTempDirectory("water-my-plants-convention-plugin-usages").toFile()
        rootDir.writeBuildFile(
            path = "app",
            content = """
            plugins {
                id("com.marmatsan.compose")
            }
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "core/ui",
            content = """
            plugins {
                id("com.marmatsan.compose")
            }
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "onboarding/ui",
            content = """
            plugins {
                id("com.marmatsan.android")
            }
            """.trimIndent()
        )
        val includedBuildRootDir = rootDir.resolve("repo/gradle-plugins")
        includedBuildRootDir.writeBuildFile(
            path = "compose",
            content = """
            gradlePlugin {
                val pluginName = "com.marmatsan.compose"
                plugins.register(pluginName) {
                    id = pluginName
                    implementationClass = "com.marmatsan.compose.plugin.ComposeGradleConventionPlugin"
                }
            }
            """.trimIndent()
        )
        includedBuildRootDir.writeKotlinFile(
            path = "compose/src/main/kotlin/com/marmatsan/compose/plugin",
            fileName = "ComposeGradleConventionPlugin.kt",
            content = """
            package com.marmatsan.compose.plugin

            fun configure(project: Project) {
                project.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            }
            """.trimIndent()
        )
        includedBuildRootDir.writeBuildFile(
            path = "android",
            content = """
            gradlePlugin {
                val pluginName = "com.marmatsan.android"
                plugins.register(pluginName) {
                    id = pluginName
                    implementationClass = "com.marmatsan.android.plugin.AndroidGradleConventionPlugin"
                }
            }
            """.trimIndent()
        )
        includedBuildRootDir.writeKotlinFile(
            path = "android/src/main/kotlin/com/marmatsan/android/plugin",
            fileName = "AndroidGradleConventionPlugin.kt",
            content = """
            package com.marmatsan.android.plugin

            fun configure(project: Project) {
                project.pluginManager.apply("com.google.devtools.ksp")
            }
            """.trimIndent()
        )

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readPluginTreeWithVersionAliases(
            rootDir = rootDir,
            conventionPluginIncludedBuilds = listOf(
                IncludedBuildSource(
                    settingsFilePath = includedBuildRootDir.resolve("settings.gradle.kts").absolutePath,
                    rootDirPath = includedBuildRootDir.absolutePath,
                    modulePathPrefix = ":gradle-plugins",
                    publishesConventionPlugins = true
                )
            )
        )

        // THEN
        actualTree.findPlugin("org.jetbrains.kotlin.plugin.compose")
            .providedByConventionPlugins shouldBe listOf(
            PluginCatalogNode.ConventionPluginUsage(
                pluginId = "com.marmatsan.compose",
                pluginModule = ":gradle-plugins:compose",
                requiredByModules = listOf(":app", ":core:ui")
            )
        )
        actualTree.findPlugin("com.google.devtools.ksp")
            .providedByConventionPlugins shouldBe listOf(
            PluginCatalogNode.ConventionPluginUsage(
                pluginId = "com.marmatsan.android",
                pluginModule = ":gradle-plugins:android",
                requiredByModules = listOf(":onboarding:ui")
            )
        )
        actualTree.findPlugin("org.jetbrains.dokka").providedByConventionPlugins shouldBe emptyList()
    }
})

private fun dependenciesCatalogTreesReader() =
    DependenciesCatalogTreesReader(
        gradleCatalogUsageReader = GradleCatalogUsageReader()
    )

private fun LibraryCatalogTree.findArtifact(
    groupPath: String,
    artifact: String
): LibraryCatalogEntry.Artifact =
    findLibraryNode(groupPath)
        .entries
        .filterIsInstance<LibraryCatalogEntry.Artifact>()
        .first { entry -> entry.artifact == artifact }

private fun LibraryCatalogTree.findBundle(
    groupPath: String,
    alias: String
): LibraryCatalogEntry.ArtifactsBundle =
    findLibraryNode(groupPath)
        .entries
        .filterIsInstance<LibraryCatalogEntry.ArtifactsBundle>()
        .first { entry -> entry.alias == alias }

private fun LibraryCatalogTree.findLibraryNode(groupPath: String): LibraryCatalogNode {
    val segments = groupPath.split(".")

    return segments.foldIndexed(null as LibraryCatalogNode?) { index, node, segment ->
        val candidates = if (index == 0) roots else node?.children.orEmpty()
        candidates.first { candidate -> candidate.group == segment }
    } ?: error("Library group '$groupPath' was not found")
}

private fun PluginCatalogTree.findPlugin(pluginId: String): PluginCatalogNode {
    val segments = pluginId.split(".")

    return segments.foldIndexed(null as PluginCatalogNode?) { index, node, segment ->
        val candidates = if (index == 0) roots else node?.children.orEmpty()
        candidates.first { candidate -> candidate.id == segment }
    } ?: error("Plugin '$pluginId' was not found")
}

private fun File.writeSettingsFile(
    path: String,
    content: String
) {
    val directory = resolve(path)
    directory.mkdirs()
    directory.resolve("settings.gradle.kts").writeText(content)
}

private fun File.writeBuildFile(
    path: String,
    content: String
) {
    val directory = resolve(path)
    directory.mkdirs()
    directory.resolve("build.gradle.kts").writeText(content)
}

private fun File.writeKotlinFile(
    path: String,
    fileName: String,
    content: String
) {
    val directory = resolve(path)
    directory.mkdirs()
    directory.resolve(fileName).writeText(content)
}
