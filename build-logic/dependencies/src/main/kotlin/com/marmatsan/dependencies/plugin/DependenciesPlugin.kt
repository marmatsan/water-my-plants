package com.marmatsan.dependencies.plugin

import com.marmatsan.dependencies.data.*
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class DependenciesPlugin : Plugin<Settings> {

    object Versions {
        const val activityComposeVersion = "1.8.2"
        const val applicationVersion = "8.2.2"
        const val assertkVersion = "0.28.0"
        const val composeVersion = "1.6.0"
        const val composeCompilerVersion = "1.5.8"
        const val coreKtxVersion = "1.12.0"
        const val coreSplashscreenVersion = "1.0.1"
        const val coilVersion = "2.5.0"
        const val datastoreVersion = "1.0.0"
        const val daggerHiltVersion = "2.50"
        const val hiltNavigationComposeVersion = "1.1.0"
        const val junit5Version = "5.10.2"
        const val kotlinVersion = "1.9.22"
        const val kotlinxSerializationJsonVersion = "1.6.2"
        const val ktorVersion = "2.3.8"
        const val kspVersion = "$kotlinVersion-1.0.17"
        const val lifecycleVersion = "2.7.0"
        const val material3Version = "1.1.2"
        const val mockkVersion = "1.13.9"
        const val protobufVersion = "0.9.4"
        const val protobufJavaliteVersion = "3.25.2"
        const val relayVersion = "0.3.11"
        const val roomVersion = "2.5.0"
        const val navigationComposeVersion = "2.7.6"
    }

    override fun apply(settings: Settings) {
        settings.dependencyResolutionManagement {
            buildVersionCatalogs()
        }
    }

}