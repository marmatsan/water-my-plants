package com.marmatsan.dependencies.plugin

import com.marmatsan.dependencies.data.buildVersionCatalogs
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class DependenciesPlugin : Plugin<Settings> {

    object Versions {
        const val APPLICATION_VERSION = "8.3.0"
        const val ACTIVITY_COMPOSE_VERSION = "1.8.2"
        const val COMPOSE_BOM_VERSION = "2024.02.02"
        const val COMPOSE_COMPILER_VERSION = "1.5.11"
        const val CORE_KTX_VERSION = "1.12.0"
        const val CORE_SPLASHSCREEN_VERSION = "1.0.1"
        const val DATASTORE_VERSION = "1.0.0"
        const val JUNIT5_VERSION = "5.10.2"
        const val KOTLIN_VERSION = "1.9.23"
        const val KOTLIN_INJECT_VERSION = "0.6.3"
        const val KSP_VERSION = "$KOTLIN_VERSION-1.0.19"
        const val LIFECYCLE_VERSION = "2.7.0"
        const val NAVIGATION_COMPOSE_VERSION = "2.7.7"
    }

    override fun apply(settings: Settings) {
        settings.dependencyResolutionManagement {
            buildVersionCatalogs()
        }
    }

}