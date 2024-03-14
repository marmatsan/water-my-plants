package com.marmatsan.dependencies.plugin

import com.marmatsan.dependencies.data.buildVersionCatalogs
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class DependenciesPlugin : Plugin<Settings> {

    object Versions {
        const val APPLICATION_VERSION = "8.3.0"
        const val ASSERTK_VERSION = "0.28.0"
        const val ACTIVITY_COMPOSE_VERSION = "1.8.2"
        const val COMPOSE_BOM_VERSION = "2024.02.02"
        const val COMPOSE_COMPILER_VERSION = "1.5.10"
        const val CORE_KTX_VERSION = "1.12.0"
        const val CORE_SPLASHSCREEN_VERSION = "1.0.1"
        const val COIL_VERSION = "2.5.0"
        const val DATASTORE_VERSION = "1.0.0"
        const val DAGGER_HILT_VERSION = "2.50"
        const val JUNIT5_VERSION = "5.10.2"
        const val KOTLIN_VERSION = "1.9.22"
        const val KOTLINX_SERIALIZATION_JSON_VERSION = "1.6.3"
        const val KTOR_VERSION = "2.3.8"
        const val KSP_VERSION = "$KOTLIN_VERSION-1.0.18"
        const val LIFECYCLE_VERSION = "2.7.0"
        const val MOCKK_VERSION = "1.13.9"
        const val PROTOBUF_VERSION = "0.9.4"
        const val PROTOBUF_JAVALITE_VERSION = "3.25.2"
    }

    override fun apply(settings: Settings) {
        settings.dependencyResolutionManagement {
            buildVersionCatalogs()
        }
    }

}