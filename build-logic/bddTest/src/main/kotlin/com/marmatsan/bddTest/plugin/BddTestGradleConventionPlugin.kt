package com.marmatsan.bddTest.plugin

import com.marmatsan.dependencies.gradle.withVersionCatalog
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
            val libs = withVersionCatalog(libs)

            libs.testImplementationPlatform(
                libraryGroup = "io.cucumber",
                artifact = "cucumber-bom"
            )
            libs.testImplementation(
                libraryGroup = "io.cucumber",
                artifact = "cucumber-java"
            )
            libs.testImplementation(
                libraryGroup = "io.cucumber",
                artifact = "cucumber-java8"
            )
            libs.testImplementation(
                libraryGroup = "io.cucumber",
                artifact = "cucumber-junit-platform-engine"
            )
            libs.testImplementation(
                libraryGroup = "org.junit.platform",
                artifact = "junit-platform-suite"
            )
            libs.testRuntimeOnly(
                libraryGroup = "org.junit.platform",
                artifact = "junit-platform-launcher"
            )
        }
    }
}
