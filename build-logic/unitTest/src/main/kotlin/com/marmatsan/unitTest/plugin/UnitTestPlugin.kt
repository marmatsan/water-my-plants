package com.marmatsan.unitTest.plugin

import com.marmatsan.dependencies.gradle.requireDependencyNotation
import com.marmatsan.dependencies.gradle.requireBundle
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
class UnitTestPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        // Applied libs
        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

        project.tasks.withType<Test> {
            useJUnitPlatform()
            jvmArgs("-XX:+EnableDynamicAgentLoading")
        }

        project.dependencies {
            // Junit5
            testImplementation(platform(libs.requireDependencyNotation("org.junit.bom")))
            libs.requireBundle("jupiterBundle").get().forEach { dependency ->
                testImplementation(dependency)
            }
            testRuntimeOnly(libs.requireDependencyNotation("org.junit.platform.launcher"))
            // AssertK
            testImplementation(libs.requireDependencyNotation("com.willowtreeapps.assertk"))
            // Mockk
            testImplementation(libs.requireDependencyNotation("io.mockk"))
        }
    }
}
