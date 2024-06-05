package com.marmatsan.dependencies.plugin

import com.marmatsan.dependencies.data.buildVersionCatalogs
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class DependenciesPlugin : Plugin<Settings> {

    object Versions {
        const val APPLICATION_VERSION = "8.4.0"
        const val ACTIVITY_COMPOSE_VERSION = "1.9.0"
        const val ANDROID_COROUTINES_VERSION = "1.8.1"
        const val ASSERTK_VERSION = "0.28.1"
        const val COMPOSE_BOM_VERSION = "2024.05.00"
        const val COMPOSE_COMPILER_VERSION = "1.5.13"
        const val CORE_KTX_VERSION = "1.13.1"
        const val CORE_SPLASHSCREEN_VERSION = "1.1.0-rc01"
        const val DATASTORE_VERSION = "1.1.1"
        const val JUNIT5_VERSION = "5.11.0-M2"
        const val JUNIT5_PLUGIN_VERSION = "1.10.0.0"
        const val KOTLIN_VERSION = "2.0.0"
        const val KOTLIN_INJECT_VERSION = "0.6.3"
        const val KSP_VERSION = "$KOTLIN_VERSION-1.0.21"
        const val LANDSCAPIST_VERSION = "2.3.3"
        const val LIFECYCLE_VERSION = "2.8.0"
        const val MOCKK_VERSION = "1.13.11"
        const val NAVIGATION_COMPOSE_VERSION = "2.8.0-beta02"
        const val PROTOBUF_LIBRARY_VERSION = "4.26.1"
        const val PROTOBUF_PLUGIN_VERSION = "0.9.4"
        const val REALM_VERSION = "2.0.0" // TODO: Waiting for release https://github.com/realm/realm-kotlin/blob/main/CHANGELOG.md
        const val SERIALIZATION_VERSION = "1.6.3"
    }

    override fun apply(settings: Settings) {
        settings.dependencyResolutionManagement {
            buildVersionCatalogs()
        }
    }

}