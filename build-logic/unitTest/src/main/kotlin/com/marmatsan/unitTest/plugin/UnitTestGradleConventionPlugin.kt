package com.marmatsan.unitTest.plugin

import com.marmatsan.dependencies.gradle.withVersionCatalog
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
            val libs = withVersionCatalog(libs)

            // Kotest
            libs.testImplementation(
                libraryGroup = "io.kotest",
                artifact = "kotest-runner-junit5"
            )
            libs.testImplementation(
                libraryGroup = "io.kotest",
                artifact = "kotest-assertions-core"
            )
            libs.testRuntimeOnly(
                libraryGroup = "org.junit.platform",
                artifact = "junit-platform-launcher"
            )
            // Mockk
            libs.testImplementation(
                libraryGroup = "io.mockk",
                artifact = "mockk"
            )
        }
    }
}
