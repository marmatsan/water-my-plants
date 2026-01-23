package com.marmatsan.android.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.marmatsan.dependencies.gradle.getLibraryByAlias
import com.marmatsan.dependencies.gradle.implementation
import com.marmatsan.dependencies.gradle.ksp
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

class AndroidPlugin : Plugin<Project> {
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
            namespace = "com.marmatsan.${project.name}"
            compileSdk = 36
            defaultConfig {
                minSdk = 33

                when (androidExtension) {
                    is ApplicationExtension -> {
                        androidExtension.apply {
                            defaultConfig {

                                val majorVersion = 0
                                val minorVersion = 1
                                val bugfixVersion = 0

                                targetSdk = 36
                                versionCode =
                                    majorVersion * 1000 + minorVersion * 100 + bugfixVersion
                                versionName = "${majorVersion}.${minorVersion}.$bugfixVersion"
                            }
                        }
                    }
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }

                project.tasks.withType<KotlinJvmCompile>().configureEach {
                    compilerOptions.apply {
                        languageVersion.set(KotlinVersion.KOTLIN_2_0)
                        jvmTarget.set(JvmTarget.JVM_21)
                    }
                }

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            }

        }

        // Applied plugins
        project.pluginManager.apply("org.jetbrains.kotlin.android")
        project.pluginManager.apply("com.google.devtools.ksp")
        project.pluginManager.apply("de.mannodermaus.android-junit5")

        // Applied libs
        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

        project.dependencies {
            /* Android core */
            implementation(libs.getLibraryByAlias("androidx.core.core.ktx"))
            implementation(libs.getLibraryByAlias("androidx.lifecycle.lifecycle.runtime.ktx"))

            /* Dependency injection */
            ksp(libs.getLibraryByAlias("me.tatarka.inject.kotlin.inject.compiler.ksp"))
            implementation(libs.getLibraryByAlias("me.tatarka.inject.kotlin.inject.runtime"))

            /* Coroutines */
            implementation(libs.getLibraryByAlias("org.jetbrains.kotlinx.kotlinx.coroutines.android"))
        }
    }
}