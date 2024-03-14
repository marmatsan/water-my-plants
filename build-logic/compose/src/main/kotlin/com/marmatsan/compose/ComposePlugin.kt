package com.marmatsan.compose

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.marmatsan.dependencies.data.getLibrary
import com.marmatsan.dependencies.data.implementation
import com.marmatsan.dependencies.plugin.DependenciesPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.hasPlugin

class ComposePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val androidExtension = when {
            project.plugins.hasPlugin(AppPlugin::class) -> {
                project.extensions.getByType<ApplicationExtension>()
            }

            else -> {
                project.extensions.getByType<LibraryExtension>()
            }
        }

        androidExtension.apply {

            defaultConfig {
                vectorDrawables {
                    useSupportLibrary = true
                }
            }

            buildFeatures {
                compose = true
            }

            composeOptions {
                kotlinCompilerExtensionVersion = DependenciesPlugin.Versions.COMPOSE_COMPILER_VERSION
            }

        }

        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libsCompose")

        project.dependencies {
            implementation(platform("androidx.compose:compose-bom:2024.02.02"))
            implementation("androidx.compose.animation:animation")
            implementation("androidx.compose.animation:animation-core")
            implementation("androidx.compose.animation:animation-graphics")
            implementation("androidx.compose.foundation:foundation")
            implementation("androidx.compose.foundation:foundation-layout")
            implementation("androidx.compose.material:material")
            implementation("androidx.compose.material:material-icons-core")
            implementation("androidx.compose.material:material-icons-extended")
            implementation("androidx.compose.material:material-ripple")
            implementation("androidx.compose.material3:material3")
            implementation("androidx.compose.material3:material3-window-size-class")
            implementation("androidx.compose.runtime:runtime")
            implementation("androidx.compose.runtime:runtime-livedata")
            implementation("androidx.compose.runtime:runtime-rxjava2")
            implementation("androidx.compose.runtime:runtime-rxjava3")
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
            libs.libraryAliases.forEach { libraryAlias ->
                implementation(libs.getLibrary(libraryAlias))
            }
        }

    }
}