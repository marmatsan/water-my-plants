package com.marmatsan.unitTest.plugin.configuration

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

/** Configures the execution contract shared by repository unit-test tasks. */
internal class UnitTestTaskConfigurator {
    /**
     * Selects JUnit Platform and permits MockK's runtime agent attachment.
     *
     * @param project Gradle project whose current and future test tasks are configured.
     */
    fun configure(
        project: Project
    ) {
        project.tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            jvmArgs("-XX:+EnableDynamicAgentLoading")
        }
    }
}
