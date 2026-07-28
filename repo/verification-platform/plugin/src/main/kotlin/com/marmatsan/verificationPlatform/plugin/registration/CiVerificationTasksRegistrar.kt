package com.marmatsan.verificationPlatform.plugin.registration

import com.marmatsan.verificationPlatform.plugin.task.ci.GenerateCiPlanTask
import com.marmatsan.verificationPlatform.plugin.task.ci.GenerateCiTopologyPreviewTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/** Registers provider-neutral CI projections derived from the verification plan. */
internal class CiVerificationTasksRegistrar(
    private val project: Project,
    private val generateCiPlan: TaskProvider<GenerateCiPlanTask>,
) {
    /** Registers the non-authoritative CI topology preview. */
    fun register() {
        project.tasks.registerVerificationTask(
            "generateCiTopologyPreview",
            GenerateCiTopologyPreviewTask::class.java,
            "Previews provider-neutral CI lanes without changing active TeamCity jobs.",
        ) { task ->
            task.dependsOn(generateCiPlan)
            task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
            task.availableAgents.convention(
                project.providers
                    .gradleProperty("ciAvailableAgents")
                    .map(String::toInt)
                    .orElse(1),
            )
            task.outputFile.convention(
                project.layout.buildDirectory.file("reports/ci/ci-topology-preview.json"),
            )
        }
    }
}
