package com.marmatsan.dependencies

import java.io.File
import java.util.Properties

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
    val junit5PluginVersion: String,
    val kotestLibraryVersion: String,
    val kotlinInjectLibraryVersion: String,
    val kotlinVersion: String,
    val ktorLibraryVersion: String,
    val kspPluginVersion: String,
    val lifecycleLibraryVersion: String,
    val mockkLibraryVersion: String,
    val navigationComposeLibraryVersion: String,
    val protobufLibraryVersion: String,
    val protobufPluginVersion: String,
    val serializationLibraryVersion: String
) {
    companion object {
        fun load(
            rootDir: File
        ): Versions {
            val versionsFile = resolveVersionsFile(
                rootDir = rootDir
            )
            val properties = Properties().apply {
                versionsFile.inputStream().use(::load)
            }

            fun get(
                key: String
            ): String = properties.getProperty(key)
                ?: error("Missing version property '$key' in ${versionsFile.path}")

            return Versions(
                activityComposeLibraryVersion = get(
                    key = "activityComposeLibraryVersion"
                ),
                androidCoroutinesLibraryVersion = get(
                    key = "androidCoroutinesLibraryVersion"
                ),
                androidGradlePluginVersion = get(
                    key = "androidGradlePluginVersion"
                ),
                composeBomLibraryVersion = get(
                    key = "composeBomLibraryVersion"
                ),
                coreKtxLibraryVersion = get(
                    key = "coreKtxLibraryVersion"
                ),
                cucumberLibraryVersion = get(
                    key = "cucumberLibraryVersion"
                ),
                dokkaPluginVersion = get(
                    key = "dokkaPluginVersion"
                ),
                figmaCodeConnectLibraryVersion = get(
                    key = "figmaCodeConnectLibraryVersion"
                ),
                figmaCodeConnectPluginVersion = get(
                    key = "figmaCodeConnectPluginVersion"
                ),
                junit5PluginVersion = get(
                    key = "junit5PluginVersion"
                ),
                kotestLibraryVersion = get(
                    key = "kotestLibraryVersion"
                ),
                kotlinInjectLibraryVersion = get(
                    key = "kotlinInjectLibraryVersion"
                ),
                kotlinVersion = get(
                    key = "kotlinVersion"
                ),
                ktorLibraryVersion = get(
                    key = "ktorLibraryVersion"
                ),
                kspPluginVersion = get(
                    key = "kspPluginVersion"
                ),
                lifecycleLibraryVersion = get(
                    key = "lifecycleLibraryVersion"
                ),
                mockkLibraryVersion = get(
                    key = "mockkLibraryVersion"
                ),
                navigationComposeLibraryVersion = get(
                    key = "navigationComposeLibraryVersion"
                ),
                protobufLibraryVersion = get(
                    key = "protobufLibraryVersion"
                ),
                protobufPluginVersion = get(
                    key = "protobufPluginVersion"
                ),
                serializationLibraryVersion = get(
                    key = "serializationLibraryVersion"
                )
            )
        }

        private fun resolveVersionsFile(
            rootDir: File
        ): File {
            val candidates = listOf(
                rootDir.resolve(
                    relative = "repo/dependency-catalog/versions.properties"
                ),
                rootDir.resolve(
                    relative = "versions.properties"
                )
            )
            return candidates.firstOrNull { it.isFile }
                ?: error("versions.properties not found in repo/dependency-catalog or root directory")
        }
    }
}
