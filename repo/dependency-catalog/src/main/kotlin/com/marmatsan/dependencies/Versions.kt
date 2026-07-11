package com.marmatsan.dependencies

import java.io.File
import java.util.Properties

data class Versions(
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
            val versionsFile = resolveVersionsFile(rootDir)
            val properties = Properties().apply {
                versionsFile.inputStream().use(::load)
            }

            fun get(
                key: String
            ): String = properties.getProperty(key)
                ?: error("Missing version property '$key' in ${versionsFile.path}")

            return Versions(
                activityComposeLibraryVersion = get("activityComposeLibraryVersion"),
                androidCoroutinesLibraryVersion = get("androidCoroutinesLibraryVersion"),
                androidGradlePluginVersion = get("androidGradlePluginVersion"),
                composeBomLibraryVersion = get("composeBomLibraryVersion"),
                coreKtxLibraryVersion = get("coreKtxLibraryVersion"),
                cucumberLibraryVersion = get("cucumberLibraryVersion"),
                dokkaPluginVersion = get("dokkaPluginVersion"),
                figmaCodeConnectLibraryVersion = get("figmaCodeConnectLibraryVersion"),
                figmaCodeConnectPluginVersion = get("figmaCodeConnectPluginVersion"),
                junit5PluginVersion = get("junit5PluginVersion"),
                kotestLibraryVersion = get("kotestLibraryVersion"),
                kotlinInjectLibraryVersion = get("kotlinInjectLibraryVersion"),
                kotlinVersion = get("kotlinVersion"),
                ktorLibraryVersion = get("ktorLibraryVersion"),
                kspPluginVersion = get("kspPluginVersion"),
                lifecycleLibraryVersion = get("lifecycleLibraryVersion"),
                mockkLibraryVersion = get("mockkLibraryVersion"),
                navigationComposeLibraryVersion = get("navigationComposeLibraryVersion"),
                protobufLibraryVersion = get("protobufLibraryVersion"),
                protobufPluginVersion = get("protobufPluginVersion"),
                serializationLibraryVersion = get("serializationLibraryVersion")
            )
        }

        private fun resolveVersionsFile(
            rootDir: File
        ): File {
            val candidates = listOf(
                rootDir.resolve("repo/dependency-catalog/versions.properties"),
                rootDir.resolve("versions.properties")
            )
            return candidates.firstOrNull { it.isFile }
                ?: error("versions.properties not found in repo/dependency-catalog or root directory")
        }
    }
}
