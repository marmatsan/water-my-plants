package com.marmatsan.unitTest.plugin

import com.marmatsan.dependencies.gradle.getLibraryByAlias
import com.marmatsan.dependencies.gradle.testImplementation
import com.marmatsan.dependencies.gradle.testRuntimeOnly
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

class UnitTestPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        // Applied libs
        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

        project.tasks.withType<Test> {
            useJUnitPlatform()
        }

        project.dependencies {
            // Junit5
            testImplementation(platform(libs.getLibraryByAlias("org.junit.junit.bom")))
            testImplementation(libs.getLibraryByAlias("org.junit.jupiter.junit.jupiter.api"))
            testRuntimeOnly(libs.getLibraryByAlias("org.junit.jupiter.junit.jupiter.engine"))
            testRuntimeOnly(libs.getLibraryByAlias("org.junit.platform.junit.platform.launcher"))
            // Assertk
            testImplementation(libs.getLibraryByAlias("com.willowtreeapps.assertk.assertk"))
            // Mockk
            testImplementation(libs.getLibraryByAlias("io.mockk.mockk"))
        }
    }
}
