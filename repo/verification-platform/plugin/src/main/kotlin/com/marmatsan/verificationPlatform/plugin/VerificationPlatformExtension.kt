package com.marmatsan.verificationPlatform.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/** Consumer-facing DSL grouped into responsibility-specific configuration blocks. */
class VerificationPlatformExtension internal constructor(
    project: Project,
    generateCiPlan: TaskProvider<GenerateCiPlanTask>,
    checkIncludedBuildVersions: TaskProvider<CheckIncludedBuildVersionsTask>,
    checkModuleBoundaries: TaskProvider<CheckModuleBoundariesTask>,
) {
    /** Provider-neutral CI classification and task-selection policy. */
    val ciPolicy =
        CiVerificationPolicyExtension(
            project = project,
            generateCiPlan = generateCiPlan,
        )

    /** Included-build version ownership and forbidden-reference boundaries. */
    val boundaries =
        RepositoryBoundariesExtension(
            project = project,
            checkIncludedBuildVersions = checkIncludedBuildVersions,
            checkModuleBoundaries = checkModuleBoundaries,
        )

    /** Root bindings for tasks owned by included builds. */
    val taskBindings = IncludedBuildVerificationTasksExtension(project)

    /** TeamCity DSL identities and infrastructure-health configuration. */
    val teamCity = TeamCityVerificationExtension(project)

    /** Configures [ciPolicy]. */
    fun ciPolicy(
        configure: CiVerificationPolicyExtension.() -> Unit,
    ) = ciPolicy.configure()

    /** Configures [boundaries]. */
    fun boundaries(
        configure: RepositoryBoundariesExtension.() -> Unit,
    ) = boundaries.configure()

    /** Configures [taskBindings]. */
    fun taskBindings(
        configure: IncludedBuildVerificationTasksExtension.() -> Unit,
    ) = taskBindings.configure()

    /** Configures [teamCity]. */
    fun teamCity(
        configure: TeamCityVerificationExtension.() -> Unit,
    ) = teamCity.configure()
}
