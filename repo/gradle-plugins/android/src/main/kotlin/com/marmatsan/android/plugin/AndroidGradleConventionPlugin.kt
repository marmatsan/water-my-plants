package com.marmatsan.android.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.AppPlugin
import com.marmatsan.dependencies.gradle.withVersionCatalog
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.hasPlugin
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

@Suppress("unused")
class AndroidGradleConventionPlugin : Plugin<Project> {
    override fun apply(
        project: Project
    ) {
        when {
            project.plugins.hasPlugin(AppPlugin::class) -> {
                configureApplicationExtension(
                    project = project,
                    extension = project.extensions.getByType<ApplicationExtension>()
                )
            }

            else -> {
                configureLibraryExtension(
                    project = project,
                    extension = project.extensions.getByType<LibraryExtension>()
                )
            }
        }

        // Applied plugins
        project.pluginManager.apply("com.google.devtools.ksp")
        project.pluginManager.apply("de.mannodermaus.android-junit5")

        // Applied libs
        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

        project.dependencies {
            val libs =
                withVersionCatalog(
                    libs = libs
                )

            // Android core
            libs.implementation(
                libraryGroup = "androidx.core",
                artifact = "core-ktx"
            )
            libs.implementation(
                libraryGroup = "androidx.lifecycle",
                artifact = "lifecycle-runtime-ktx"
            )

            // Dependency injection
            libs.ksp(
                libraryGroup = "me.tatarka.inject",
                artifact = "kotlin-inject-compiler-ksp"
            )
            libs.implementation(
                libraryGroup = "me.tatarka.inject",
                artifact = "kotlin-inject-runtime"
            )

            // Coroutines
            libs.implementation(
                libraryGroup = "org.jetbrains.kotlinx",
                artifact = "kotlinx-coroutines-android"
            )
        }
    }

    private fun configureApplicationExtension(
        project: Project,
        extension: ApplicationExtension
    ) {
        extension.apply {
            namespace = "com.marmatsan.${project.name}"
            compileSdk = 37
            defaultConfig {
                val majorVersion = 0
                val minorVersion = 1
                val bugfixVersion = 0

                minSdk = 33
                targetSdk = 37
                @Suppress("KotlinConstantConditions")
                versionCode = majorVersion * 1000 + minorVersion * 100 + bugfixVersion
                versionName = "$majorVersion.$minorVersion.$bugfixVersion"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
        }
        configureKotlin(
            project = project
        )
    }

    private fun configureLibraryExtension(
        project: Project,
        extension: LibraryExtension
    ) {
        extension.apply {
            namespace = "com.marmatsan.${project.name}"
            compileSdk = 37
            defaultConfig {
                minSdk = 33
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
        }
        configureKotlin(
            project = project
        )
    }

    private fun configureKotlin(
        project: Project
    ) {
        project.tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions.apply {
                languageVersion.set(KotlinVersion.KOTLIN_2_3)
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }
    }
}
