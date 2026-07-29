package com.marmatsan.compose.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.AppPlugin
import com.marmatsan.dependencies.gradle.withVersionCatalog
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.hasPlugin

@Suppress("unused")
class ComposeGradleConventionPlugin : Plugin<Project> {
    override fun apply(
        project: Project
    ) {
        when {
            project.plugins.hasPlugin(AppPlugin::class) -> {
                configureApplicationExtension(
                    extension = project.extensions.getByType<ApplicationExtension>()
                )
            }

            else -> {
                configureLibraryExtension(
                    extension = project.extensions.getByType<LibraryExtension>()
                )
            }
        }

        project.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        if (project.providers
                .gradleProperty("figmaCodeConnectEnabled")
                .map(
                    String::toBoolean
                ).getOrElse(false)
        ) {
            project.pluginManager.apply("com.figma.code.connect")
        }

        // Applied libs
        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

        project.dependencies {
            val libs =
                withVersionCatalog(
                    libs = libs
                )

            // Compose libraries managed by Compose BOM
            libs.implementationPlatform(
                libraryGroup = "androidx.compose",
                artifact = "compose-bom"
            )
            libs.implementation(
                libraryGroup = "androidx.compose.material3",
                artifact = "material3"
            )
            libs.implementation(
                libraryGroup = "androidx.compose.material",
                artifact = "material-icons-core"
            )
            libs.implementationBundle(
                bundle = "composeBundle"
            )

            // Other Compose libraries
            libs.implementation(
                libraryGroup = "androidx.activity",
                artifact = "activity-compose"
            )
            libs.implementationBundle(
                bundle = "lifecycleCompose"
            )
            libs.implementation(
                libraryGroup = "androidx.navigation",
                artifact = "navigation-compose"
            )

            // Figma Code Connect
            libs.implementation(
                libraryGroup = "com.figma.code.connect",
                artifact = "code-connect-lib"
            )
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
