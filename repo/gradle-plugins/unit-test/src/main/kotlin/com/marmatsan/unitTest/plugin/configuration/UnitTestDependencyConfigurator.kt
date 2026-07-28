package com.marmatsan.unitTest.plugin.configuration

import com.marmatsan.dependencies.gradle.withVersionCatalog
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/** Adds the shared assertion, mocking, engine, and typed behavior dependencies. */
internal class UnitTestDependencyConfigurator {
    /**
     * Adds dependencies to the consumer's unit-test configurations.
     *
     * Product dependency versions remain owned by the consumer's `libs` catalog.
     * The typed behavior API is resolved from the consumer's `testLibs` catalog,
     * keeping repository test tooling outside the product catalog.
     *
     * @param project Gradle project receiving the shared test dependencies.
     */
    fun configure(
        project: Project,
    ) {
        val versionCatalogs = project.extensions.getByType<VersionCatalogsExtension>()
        val productLibraries = versionCatalogs.named("libs")
        val testLibraries = versionCatalogs.named("testLibs")

        project.dependencies {
            val catalogDependencies =
                withVersionCatalog(
                    libs = productLibraries,
                )

            catalogDependencies.testImplementation(
                libraryGroup = "io.kotest",
                artifact = "kotest-runner-junit5",
            )
            catalogDependencies.testImplementation(
                libraryGroup = "io.kotest",
                artifact = "kotest-assertions-core",
            )
            catalogDependencies.testRuntimeOnly(
                libraryGroup = "org.junit.platform",
                artifact = "junit-platform-launcher",
            )
            catalogDependencies.testImplementation(
                libraryGroup = "io.mockk",
                artifact = "mockk",
            )
            withVersionCatalog(
                libs = testLibraries,
            ).testImplementation(
                libraryGroup = "com.marmatsan.repo",
                artifact = "unit-test-dsl",
            )
        }
    }
}
