package com.marmatsan.unitTest.plugin

import com.marmatsan.unitTest.plugin.configuration.UnitTestDependencyConfigurator
import com.marmatsan.unitTest.plugin.configuration.UnitTestTaskConfigurator
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Composes the repository's unit-test task and dependency conventions.
 *
 * Runtime configuration is delegated to focused collaborators so this Gradle
 * entry point owns only implementation selection and orchestration.
 */
@Suppress("unused")
class UnitTestGradleConventionPlugin : Plugin<Project> {
    override fun apply(
        project: Project,
    ) {
        UnitTestTaskConfigurator().configure(project)
        UnitTestDependencyConfigurator().configure(project)
    }
}
