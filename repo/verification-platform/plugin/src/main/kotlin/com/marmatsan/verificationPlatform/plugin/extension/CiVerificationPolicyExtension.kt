package com.marmatsan.verificationPlatform.plugin.extension

import com.marmatsan.verificationPlatform.plugin.task.ci.GenerateCiPlanTask
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.TaskProvider

/** Configures repository-specific classification and verification selection for the CI planner. */
class CiVerificationPolicyExtension internal constructor(
    project: Project,
    generateCiPlan: TaskProvider<GenerateCiPlanTask>,
) {
    /** Path prefixes classified as repository tooling. */
    val toolingPathPrefixes: ListProperty<String> = project.objects.listProperty(String::class.java)

    /** Path prefixes classified as build infrastructure. */
    val buildInfrastructurePathPrefixes: ListProperty<String> = project.objects.listProperty(String::class.java)

    /** Exact paths classified as build infrastructure. */
    val buildInfrastructurePaths: ListProperty<String> = project.objects.listProperty(String::class.java)

    /** Path prefixes that require staged portable-distribution verification. */
    val portableDistributionPathPrefixes: ListProperty<String> = project.objects.listProperty(String::class.java)

    /** Exact paths that require staged portable-distribution verification. */
    val portableDistributionPaths: ListProperty<String> = project.objects.listProperty(String::class.java)

    /** Agent capabilities required by repository tooling verification. */
    val toolingCapabilities: ListProperty<String> = project.objects.listProperty(String::class.java)

    /** Agent capabilities required by build-infrastructure verification. */
    val buildInfrastructureCapabilities: ListProperty<String> = project.objects.listProperty(String::class.java)

    /** Agent capabilities required by staged portable-distribution verification. */
    val portableDistributionCapabilities: ListProperty<String> = project.objects.listProperty(String::class.java)

    /** Reviewed Gradle tasks selected for repository-tooling changes. */
    val toolingVerificationTasks: ListProperty<String> = project.objects.listProperty(String::class.java)

    /** Reviewed Gradle tasks selected for build-infrastructure changes. */
    val buildInfrastructureVerificationTasks: ListProperty<String> = project.objects.listProperty(String::class.java)

    /** Reviewed Gradle tasks selected for portable-distribution changes. */
    val portableDistributionVerificationTasks: ListProperty<String> = project.objects.listProperty(String::class.java)

    /** Repository-wide tasks added to targeted module verification. */
    val targetedModuleSupplementalTasks: ListProperty<String> = project.objects.listProperty(String::class.java)

    init {
        listOf(
            toolingPathPrefixes,
            buildInfrastructurePathPrefixes,
            buildInfrastructurePaths,
            portableDistributionPathPrefixes,
            portableDistributionPaths,
            toolingCapabilities,
            buildInfrastructureCapabilities,
            portableDistributionCapabilities,
            toolingVerificationTasks,
            buildInfrastructureVerificationTasks,
            portableDistributionVerificationTasks,
            targetedModuleSupplementalTasks,
        ).forEach { property -> property.convention(emptyList()) }

        generateCiPlan.configure { task ->
            task.toolingPathPrefixes.set(toolingPathPrefixes)
            task.buildInfrastructurePathPrefixes.set(buildInfrastructurePathPrefixes)
            task.buildInfrastructurePaths.set(buildInfrastructurePaths)
            task.portableDistributionPathPrefixes.set(portableDistributionPathPrefixes)
            task.portableDistributionPaths.set(portableDistributionPaths)
            task.toolingCapabilities.set(toolingCapabilities)
            task.buildInfrastructureCapabilities.set(buildInfrastructureCapabilities)
            task.portableDistributionCapabilities.set(portableDistributionCapabilities)
            task.toolingVerificationTasks.set(toolingVerificationTasks)
            task.buildInfrastructureVerificationTasks.set(buildInfrastructureVerificationTasks)
            task.portableDistributionVerificationTasks.set(portableDistributionVerificationTasks)
            task.targetedModuleSupplementalTasks.set(targetedModuleSupplementalTasks)
        }
    }
}
