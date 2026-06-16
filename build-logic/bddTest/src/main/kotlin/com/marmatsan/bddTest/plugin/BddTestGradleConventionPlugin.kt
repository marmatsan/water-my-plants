package com.marmatsan.bddTest.plugin

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
class BddTestGradleConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

        project.tasks.withType<Test> {
            useJUnitPlatform()
            systemProperty("cucumber.junit-platform.naming-strategy", "long")
            systemProperty(
                "cucumber.plugin",
                "pretty,html:build/reports/cucumber/cucumber.html,json:build/reports/cucumber/cucumber.json"
            )

            System.getProperty("cucumber.filter.tags")?.let { tags ->
                systemProperty("cucumber.filter.tags", tags)
            }
            System.getProperty("cucumber.features")?.let { features ->
                systemProperty("cucumber.features", features)
            }
        }

        project.dependencies {
            testImplementation(platform(libs.requireDependencyNotation("io.cucumber.bom")))
            testImplementation(libs.requireDependencyNotation("io.cucumber.java"))
            testImplementation(libs.requireDependencyNotation("io.cucumber.junit.platform.engine"))
            testImplementation(libs.requireDependencyNotation("org.junit.platform.suite"))
            testRuntimeOnly(libs.requireDependencyNotation("org.junit.platform.launcher"))
        }
    }
}
