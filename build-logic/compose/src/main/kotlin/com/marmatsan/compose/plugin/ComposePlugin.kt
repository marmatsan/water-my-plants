package com.marmatsan.compose.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.AppPlugin
import com.marmatsan.dependencies.gradle.requireLibraryNotation
import com.marmatsan.dependencies.gradle.implementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.hasPlugin

class ComposePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        when {
            project.plugins.hasPlugin(AppPlugin::class) -> {
                configureApplicationExtension(project.extensions.getByType<ApplicationExtension>())
            }

            else -> {
                configureLibraryExtension(project.extensions.getByType<LibraryExtension>())
            }
        }

        project.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        if (project.providers.gradleProperty("figmaCodeConnectEnabled").map(String::toBoolean).getOrElse(false)) {
            project.pluginManager.apply("com.figma.code.connect")
        }

        // Applied libs
        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

        project.dependencies {
            /* Compose libraries managed by Compose BOM */
            implementation(platform(libs.requireLibraryNotation("androidx.compose.compose.bom")))
            implementation("androidx.compose.animation:animation")
            implementation("androidx.compose.animation:animation-core")
            implementation("androidx.compose.animation:animation-graphics")
            implementation("androidx.compose.foundation:foundation")
            implementation("androidx.compose.foundation:foundation-layout")
            implementation("androidx.compose.material:material-icons-core")
            implementation("androidx.compose.material:material-icons-extended")
            implementation("androidx.compose.material:material-ripple")
            implementation("androidx.compose.material3:material3")
            implementation("androidx.compose.material3:material3-window-size-class")
            implementation("androidx.compose.runtime:runtime")
            implementation("androidx.compose.runtime:runtime-saveable")
            implementation("androidx.compose.ui:ui")
            implementation("androidx.compose.ui:ui-geometry")
            implementation("androidx.compose.ui:ui-graphics")
            implementation("androidx.compose.ui:ui-test")
            implementation("androidx.compose.ui:ui-test-junit4")
            implementation("androidx.compose.ui:ui-test-manifest")
            implementation("androidx.compose.ui:ui-text")
            implementation("androidx.compose.ui:ui-text-google-fonts")
            implementation("androidx.compose.ui:ui-tooling")
            implementation("androidx.compose.ui:ui-tooling-data")
            implementation("androidx.compose.ui:ui-tooling-preview")
            implementation("androidx.compose.ui:ui-unit")
            implementation("androidx.compose.ui:ui-util")
            implementation("androidx.compose.ui:ui-viewbinding")

            /* Other Compose libraries */
            implementation(libs.requireLibraryNotation("androidx.activity.activity.compose"))
            implementation(libs.requireLibraryNotation("androidx.lifecycle.lifecycle.viewmodel.compose"))
            implementation(libs.requireLibraryNotation("androidx.lifecycle.lifecycle.runtime.compose"))
            implementation(libs.requireLibraryNotation("androidx.navigation.navigation.compose"))

            /* Figma Code Connect */
            implementation(libs.requireLibraryNotation("com.figma.code.connect.code.connect.lib"))
        }
    }

    private fun configureApplicationExtension(
        extension: ApplicationExtension
    ) {
        extension.apply {
            defaultConfig {
                vectorDrawables {
                    useSupportLibrary = true
                }
            }

            buildFeatures {
                compose = true
            }
        }
    }

    private fun configureLibraryExtension(
        extension: LibraryExtension
    ) {
        extension.apply {
            defaultConfig {
                vectorDrawables {
                    useSupportLibrary = true
                }
            }

            buildFeatures {
                compose = true
            }
        }
    }
}
