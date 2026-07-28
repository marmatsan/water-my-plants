package com.marmatsan.waterMyPlants.projectConfig.catalog

import java.io.File
import java.util.Properties

/**
 * Typed snapshot of the Water My Plants version properties consumed by catalog construction.
 *
 * @property activityComposeLibraryVersion AndroidX Activity Compose version.
 * @property androidCoroutinesLibraryVersion Kotlin coroutines Android version.
 * @property androidGradlePluginVersion Android Gradle Plugin version.
 * @property composeBomLibraryVersion Jetpack Compose BOM version.
 * @property coreKtxLibraryVersion AndroidX Core KTX version.
 * @property cucumberLibraryVersion Cucumber JVM version.
 * @property dokkaPluginVersion Dokka Gradle plugin version.
 * @property figmaCodeConnectLibraryVersion Figma Code Connect runtime version.
 * @property figmaCodeConnectPluginVersion Figma Code Connect Gradle plugin version.
 * @property gradleConventionPluginVersion Repository-owned Gradle convention plugins version.
 * @property junit5PluginVersion JUnit Platform Gradle plugin version.
 * @property kotestLibraryVersion Kotest library version.
 * @property kotlinInjectLibraryVersion Kotlin Inject library version.
 * @property kotlinVersion Kotlin compiler and Gradle plugin version.
 * @property kspPluginVersion Kotlin Symbol Processing plugin version.
 * @property lifecycleLibraryVersion AndroidX Lifecycle version.
 * @property mockkLibraryVersion MockK library version.
 * @property navigationComposeLibraryVersion Navigation Compose version.
 * @property protobufLibraryVersion Protocol Buffers runtime and compiler version.
 * @property protobufPluginVersion Protocol Buffers Gradle plugin version.
 */
internal data class Versions(
    val activityComposeLibraryVersion: String,
    val androidCoroutinesLibraryVersion: String,
    val androidGradlePluginVersion: String,
    val composeBomLibraryVersion: String,
    val coreKtxLibraryVersion: String,
    val cucumberLibraryVersion: String,
    val dokkaPluginVersion: String,
    val figmaCodeConnectLibraryVersion: String,
    val figmaCodeConnectPluginVersion: String,
    val gradleConventionPluginVersion: String,
    val junit5PluginVersion: String,
    val kotestLibraryVersion: String,
    val kotlinInjectLibraryVersion: String,
    val kotlinVersion: String,
    val kspPluginVersion: String,
    val lifecycleLibraryVersion: String,
    val mockkLibraryVersion: String,
    val navigationComposeLibraryVersion: String,
    val protobufLibraryVersion: String,
    val protobufPluginVersion: String,
) {
    /** Loads the repository-owned version source used by the production catalog. */
    companion object {
        /** Resolves and parses `versions.properties` relative to [rootDir]. */
        fun load(
            rootDir: File,
        ): Versions {
            val versionsFile =
                resolveVersionsFile(
                    rootDir = rootDir,
                )
            val properties =
                Properties().apply {
                    versionsFile.inputStream().use(::load)
                }

            fun get(
                key: String,
            ): String =
                properties.getProperty(key)
                    ?: error("Missing version property '$key' in ${versionsFile.path}")

            return Versions(
                activityComposeLibraryVersion =
                    get(
                        key = "activityComposeLibraryVersion",
                    ),
                androidCoroutinesLibraryVersion =
                    get(
                        key = "androidCoroutinesLibraryVersion",
                    ),
                androidGradlePluginVersion =
                    get(
                        key = "androidGradlePluginVersion",
                    ),
                composeBomLibraryVersion =
                    get(
                        key = "composeBomLibraryVersion",
                    ),
                coreKtxLibraryVersion =
                    get(
                        key = "coreKtxLibraryVersion",
                    ),
                cucumberLibraryVersion =
                    get(
                        key = "cucumberLibraryVersion",
                    ),
                dokkaPluginVersion =
                    get(
                        key = "dokkaPluginVersion",
                    ),
                figmaCodeConnectLibraryVersion =
                    get(
                        key = "figmaCodeConnectLibraryVersion",
                    ),
                figmaCodeConnectPluginVersion =
                    get(
                        key = "figmaCodeConnectPluginVersion",
                    ),
                gradleConventionPluginVersion =
                    get(
                        key = "gradleConventionPluginVersion",
                    ),
                junit5PluginVersion =
                    get(
                        key = "junit5PluginVersion",
                    ),
                kotestLibraryVersion =
                    get(
                        key = "kotestLibraryVersion",
                    ),
                kotlinInjectLibraryVersion =
                    get(
                        key = "kotlinInjectLibraryVersion",
                    ),
                kotlinVersion =
                    get(
                        key = "kotlinVersion",
                    ),
                kspPluginVersion =
                    get(
                        key = "kspPluginVersion",
                    ),
                lifecycleLibraryVersion =
                    get(
                        key = "lifecycleLibraryVersion",
                    ),
                mockkLibraryVersion =
                    get(
                        key = "mockkLibraryVersion",
                    ),
                navigationComposeLibraryVersion =
                    get(
                        key = "navigationComposeLibraryVersion",
                    ),
                protobufLibraryVersion =
                    get(
                        key = "protobufLibraryVersion",
                    ),
                protobufPluginVersion =
                    get(
                        key = "protobufPluginVersion",
                    ),
            )
        }

        private fun resolveVersionsFile(
            rootDir: File,
        ): File {
            val candidates =
                listOf(
                    rootDir.resolve(
                        relative = "repo/water-my-plants-project-config/versions.properties",
                    ),
                    rootDir.resolve(
                        relative = "versions.properties",
                    ),
                )
            return candidates.firstOrNull { it.isFile }
                ?: error("versions.properties not found in repo/water-my-plants-project-config or root directory")
        }
    }
}
