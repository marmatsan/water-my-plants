package com.marmatsan.verificationPlatform.plugin.registration

import com.marmatsan.verificationPlatform.plugin.task.ci.GenerateCiPlanTask
import com.marmatsan.verificationPlatform.plugin.task.documentation.CheckDocumentationTask
import com.marmatsan.verificationPlatform.plugin.task.git.CheckGitWorkflowTask
import com.marmatsan.verificationPlatform.plugin.task.git.CheckRepositoryDiffTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/** Registers Git and documentation tasks that verify the repository contract. */
internal class RepositoryVerificationTasksRegistrar(
    private val project: Project,
    private val generateCiPlan: TaskProvider<GenerateCiPlanTask>
) {
    /** Registers repository checks and returns the shared documentation gate. */
    fun register(): TaskProvider<CheckDocumentationTask> {
        val checkGitWorkflow =
            project.tasks.registerVerificationTask(
                "checkGitWorkflow",
                CheckGitWorkflowTask::class.java,
                "Validates the current branch against the trunk-based Git workflow."
            ) { task ->
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.branchOverride.convention(
                    project.providers
                        .gradleProperty("gitWorkflowBranch")
                        .orElse(project.providers.environmentVariable("GIT_WORKFLOW_BRANCH"))
                )
            }

        val checkDocumentation =
            project.tasks.registerVerificationTask(
                "checkDocumentation",
                CheckDocumentationTask::class.java,
                "Validates typed documentation, links, sources, and committed change coverage."
            ) { task ->
                task.dependsOn(checkGitWorkflow)
                task.dependsOn(generateCiPlan)
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.coverageManifest.set(project.layout.projectDirectory.file(".teamcity/documentation-coverage.json"))
                task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
            }

        project.tasks.registerVerificationTask(
            "checkRepositoryDiff",
            CheckRepositoryDiffTask::class.java,
            "Checks committed documentation-only diffs for whitespace errors."
        ) { task ->
            task.dependsOn(checkDocumentation)
            task.repositoryRoot.set(project.layout.projectDirectory)
            task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
        }

        return checkDocumentation
    }
}
