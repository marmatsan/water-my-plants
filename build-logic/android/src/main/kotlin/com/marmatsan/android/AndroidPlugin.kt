package com.marmatsan.android

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.marmatsan.dependencies.data.getLibrary
import com.marmatsan.dependencies.data.implementation
import com.marmatsan.dependencies.data.ksp
import com.marmatsan.dependencies.data.testImplementation
import com.marmatsan.dependencies.data.testRuntimeOnly
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
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
            compileSdk = 34
            defaultConfig {
                minSdk = 26

                when (androidExtension) {
                    is ApplicationExtension -> {
                        androidExtension.apply {
                            defaultConfig {

                                val majorVersion = 1
                                val minorVersion = 0
                                val bugfixVersion = 0

                                targetSdk = 34
                                versionCode =
                                    majorVersion * 1000 + minorVersion * 100 + bugfixVersion
                                versionName = "${majorVersion}.${minorVersion}.$bugfixVersion"
                            }
                        }
                    }
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                project.tasks.withType<KotlinJvmCompile>().configureEach {
                    compilerOptions.apply {
                        languageVersion.set(KotlinVersion.KOTLIN_1_9)
                        jvmTarget.set(JvmTarget.JVM_17)
                        freeCompilerArgs.set(listOf("-Xcontext-receivers"))
                    }
                }


                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            }

        }
        // Applied plugins
        project.pluginManager.apply("org.jetbrains.kotlin.android")
        project.pluginManager.apply("com.google.devtools.ksp")
        project.pluginManager.apply("io.realm.kotlin")
        project.pluginManager.apply("de.mannodermaus.android-junit5")

        // Applied libs
        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

        project.dependencies {
            /* Dependency injection */
            ksp(libs.getLibrary("me.tatarka.inject.kotlin.inject.compiler.ksp"))
            implementation(libs.getLibrary("me.tatarka.inject.kotlin.inject.runtime"))

            /* Coroutines */
            implementation(libs.getLibrary("org.jetbrains.kotlinx.kotlinx.coroutines.android"))

            /* Realm database */
            implementation(libs.getLibrary("io.realm.kotlin"))

            /* Testing */
            // Junit5
            testImplementation(libs.getLibrary("org.junit.jupiter.junit.jupiter.api"))
            testImplementation(libs.getLibrary("org.junit.jupiter.junit.jupiter.params"))
            testRuntimeOnly(libs.getLibrary("org.junit.jupiter.junit.jupiter.engine"))
            // Assertk
            testImplementation(libs.getLibrary("com.willowtreeapps.assertk"))
            // Mockk
            testImplementation(libs.getLibrary("io.mockk"))
        }

        project.tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}