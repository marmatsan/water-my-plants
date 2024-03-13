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
                kotlinCompilerExtensionVersion =
                    DependenciesPlugin.Versions.COMPOSE_COMPILER_VERSION
            }

        }

        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

        libs.libraryAliases.forEach {
            println(it)
        }

        project.dependencies {
            implementation(platform(libs.getLibrary("androidx.compose")))
        }

    }
}