package com.marmatsan.dependencies.plugin

import com.marmatsan.dependencies.gradle.configureVersionCatalogs
import com.marmatsan.dependencies.version.Versions
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class DependenciesPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.dependencyResolutionManagement {
            val versions = Versions.load(settings.rootDir)
            configureVersionCatalogs(versions)
        }
    }
}