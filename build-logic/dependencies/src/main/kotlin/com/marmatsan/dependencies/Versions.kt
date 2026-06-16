package com.marmatsan.dependencies

import java.io.File
import java.util.Properties

data class Versions(
    val activityComposeVersion: String,
    val androidCoroutinesVersion: String,
    val androidGradlePlugin: String,
    val composeBomVersion: String,
    val coreKtxVersion: String,
    val coreSplashscreenVersion: String,
    val cucumberVersion: String,
    val datastoreVersion: String,
    val figmaCodeConnectLibraryVersion: String,
    val figmaCodeConnectPluginVersion: String,
    val junit5PluginVersion: String,
    val kotestVersion: String,
    val kotlinxKoverVersion: String,
    val kotlinInjectVersion: String,
    val kotlinVersion: String,
    val ktorVersion: String,
    val kspVersion: String,
    val landscapistVersion: String,
    val lifecycleVersion: String,
    val mockkVersion: String,
    val navigationComposeVersion: String,
    val protobufLibraryVersion: String,
    val protobufPluginVersion: String,
    val serializationVersion: String
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
                activityComposeVersion = get("activityComposeVersion"),
                androidCoroutinesVersion = get("androidCoroutinesVersion"),
                androidGradlePlugin = get("androidGradlePlugin"),
                composeBomVersion = get("composeBomVersion"),
                coreKtxVersion = get("coreKtxVersion"),
                coreSplashscreenVersion = get("coreSplashscreenVersion"),
                cucumberVersion = get("cucumberVersion"),
                datastoreVersion = get("datastoreVersion"),
                figmaCodeConnectLibraryVersion = get("figmaCodeConnectLibraryVersion"),
                figmaCodeConnectPluginVersion = get("figmaCodeConnectPluginVersion"),
                junit5PluginVersion = get("junit5PluginVersion"),
                kotestVersion = get("kotestVersion"),
                kotlinxKoverVersion = get("kotlinxKoverVersion"),
                kotlinInjectVersion = get("kotlinInjectVersion"),
                kotlinVersion = get("kotlinVersion"),
                ktorVersion = get("ktorVersion"),
                kspVersion = get("kspVersion"),
                landscapistVersion = get("landscapistVersion"),
                lifecycleVersion = get("lifecycleVersion"),
                mockkVersion = get("mockkVersion"),
                navigationComposeVersion = get("navigationComposeVersion"),
                protobufLibraryVersion = get("protobufLibraryVersion"),
                protobufPluginVersion = get("protobufPluginVersion"),
                serializationVersion = get("serializationVersion")
            )
        }

        private fun resolveVersionsFile(
            rootDir: File
        ): File {
            val candidates = listOf(
                rootDir.resolve("build-logic/versions.properties"),
                rootDir.resolve("versions.properties")
            )
            return candidates.firstOrNull { it.isFile }
                ?: error("versions.properties not found in build-logic or root directory")
        }
    }
}
