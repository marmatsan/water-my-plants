package com.marmatsan.unitTest.plugin

import com.marmatsan.dependencies.gradle.requireDependencyNotation
import com.marmatsan.dependencies.gradle.testImplementation
import com.marmatsan.dependencies.gradle.testRuntimeOnly
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

@Suppress("unused")
class UnitTestGradleConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        // Applied libs
        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

        project.tasks.withType<Test> {
            useJUnitPlatform()
            jvmArgs("-XX:+EnableDynamicAgentLoading")
        }

        project.dependencies {
            // Kotest
            testImplementation(
                libs.requireDependencyNotation(
                    libraryGroup = "io.kotest",
                    artifact = "kotest-runner-junit5"
                )
            )
            testImplementation(
                libs.requireDependencyNotation(
                    libraryGroup = "io.kotest",
                    artifact = "kotest-assertions-core"
                )
            )
            testRuntimeOnly(libs.requireDependencyNotation("org.junit.platform.launcher"))
            // Mockk
            testImplementation(libs.requireDependencyNotation("io.mockk"))
        }
    }
}
