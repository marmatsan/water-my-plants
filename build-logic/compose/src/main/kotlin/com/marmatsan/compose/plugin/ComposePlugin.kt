package com.marmatsan.compose.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.AppPlugin
import com.marmatsan.dependencies.gradle.requireDependencyNotation
import com.marmatsan.dependencies.gradle.implementation
import com.marmatsan.dependencies.gradle.requireBundle
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
            implementation(platform(libs.requireDependencyNotation("androidx.compose.bom")))
            implementation("androidx.compose.material3:material3")
            libs.requireBundle("composeBundle").get().forEach { dependency ->
                implementation(dependency)
            }

            /* Other Compose libraries */
            implementation(libs.requireDependencyNotation("androidx.activity.compose"))
            implementation(libs.requireDependencyNotation("androidx.lifecycle.viewmodel.compose"))
            implementation(libs.requireDependencyNotation("androidx.lifecycle.runtime.compose"))
            implementation(libs.requireDependencyNotation("androidx.navigation.compose"))

            /* Figma Code Connect */
            implementation(libs.requireDependencyNotation("com.figma.code.connect.lib"))
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
