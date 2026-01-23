package com.marmatsan.dependencies.version

import java.io.File
import java.util.Properties

data class Versions(
    val applicationVersion: String,
    val kotlinVersion: String,
    val junit5BomVersion: String,
    val junit5PluginVersion: String,
    val mockkVersion: String,
    val assertkVersion: String,
    val activityComposeVersion: String,
    val androidCoroutinesVersion: String,
    val composeBomVersion: String,
    val composeCompilerVersion: String,
    val coreKtxVersion: String,
    val coreSplashscreenVersion: String,
    val datastoreVersion: String,
    val kotlinInjectVersion: String,
    val kspVersion: String,
    val landscapistVersion: String,
    val lifecycleVersion: String,
    val navigationComposeVersion: String,
    val protobufLibraryVersion: String,
    val protobufPluginVersion: String,
    val serializationVersion: String
) {
    companion object {
        fun load(rootDir: File): Versions {
            val versionsFile = resolveVersionsFile(rootDir)
            val properties = Properties().apply {
                versionsFile.inputStream().use(::load)
            }

            fun get(key: String): String =
                properties.getProperty(key)
                    ?: error("Missing version property '$key' in ${versionsFile.path}")

            return Versions(
                applicationVersion = get("applicationVersion"),
                kotlinVersion = get("kotlinVersion"),
                junit5BomVersion = get("junit5BomVersion"),
                junit5PluginVersion = get("junit5PluginVersion"),
                mockkVersion = get("mockkVersion"),
                assertkVersion = get("assertkVersion"),
                activityComposeVersion = get("activityComposeVersion"),
                androidCoroutinesVersion = get("androidCoroutinesVersion"),
                composeBomVersion = get("composeBomVersion"),
                composeCompilerVersion = get("composeCompilerVersion"),
                coreKtxVersion = get("coreKtxVersion"),
                coreSplashscreenVersion = get("coreSplashscreenVersion"),
                datastoreVersion = get("datastoreVersion"),
                kotlinInjectVersion = get("kotlinInjectVersion"),
                kspVersion = get("kspVersion"),
                landscapistVersion = get("landscapistVersion"),
                lifecycleVersion = get("lifecycleVersion"),
                navigationComposeVersion = get("navigationComposeVersion"),
                protobufLibraryVersion = get("protobufLibraryVersion"),
                protobufPluginVersion = get("protobufPluginVersion"),
                serializationVersion = get("serializationVersion")
            )
        }

        private fun resolveVersionsFile(rootDir: File): File {
            val candidates = listOf(
                rootDir.resolve("build-logic/versions.properties"),
                rootDir.resolve("versions.properties")
            )
            return candidates.firstOrNull { it.isFile }
                ?: error("versions.properties not found in build-logic or root directory")
        }
    }
}
