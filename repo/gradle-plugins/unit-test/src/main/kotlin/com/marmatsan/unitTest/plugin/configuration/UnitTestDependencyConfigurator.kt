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
     * The typed behavior API is resolved from its repository-owned module coordinate.
     *
     * @param project Gradle project receiving the shared test dependencies.
     */
    fun configure(
        project: Project,
    ) {
        val versionCatalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

        project.dependencies {
            val catalogDependencies =
                withVersionCatalog(
                    libs = versionCatalog,
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
            add(
                "testImplementation",
                UNIT_TEST_DSL_DEPENDENCY,
            )
        }
    }

    private companion object {
        const val UNIT_TEST_DSL_DEPENDENCY = "com.marmatsan.repo:unit-test-dsl"
    }
}
