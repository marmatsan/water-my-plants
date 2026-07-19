package com.marmatsan.ci.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class CiGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        require(project == project.rootProject) {
            "com.marmatsan.ci must be applied to the root project."
        }

        project.tasks.register("generateCiPlan", GenerateCiPlanTask::class.java) { task ->
            task.group = "verification"
            task.description = "Generates the provider-neutral CI verification plan."
            task.repositoryRoot.set(project.layout.projectDirectory)
            project.providers.gradleProperty("ciComparisonBase").orNull?.let(task.comparisonBaseOverride::set)
            task.outputFile.convention(project.layout.buildDirectory.file("reports/ci/ci-plan.json"))
        }
    }
}
