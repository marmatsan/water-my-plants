package com.marmatsan.verificationPlatform.plugin

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider

/**
 * Configures repository-specific policy and task bindings for the reusable verification platform.
 *
 * A composition root uses this extension to name concrete included builds and
 * source paths. The verification implementation therefore depends only on this
 * public configuration API.
 */
class VerificationPlatformExtension internal constructor(
    private val project: Project,
    private val generateCiPlan: TaskProvider<GenerateCiPlanTask>,
    private val checkIncludedBuildVersions: TaskProvider<CheckIncludedBuildVersionsTask>,
    private val checkModuleBoundaries: TaskProvider<CheckModuleBoundariesTask>,
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

    /** TeamCity build type queued by the optional infrastructure-health task. */
    val infrastructureHealthBuildTypeId: Property<String> = project.objects.property(String::class.java)

    /** Maven project that generates the consuming repository's TeamCity configuration. */
    val teamCityPom: RegularFileProperty = project.objects.fileProperty()

    /** Directory receiving generated TeamCity configuration. */
    val teamCityGeneratedConfigurationDirectory: DirectoryProperty = project.objects.directoryProperty()

    /** TeamCity build type id of the generated pipeline head. */
    val teamCityPipelineBuildTypeId: Property<String> = project.objects.property(String::class.java)

    /** TeamCity build type id of the authoritative composite gate. */
    val teamCityGateBuildTypeId: Property<String> = project.objects.property(String::class.java)

    /** GitHub status name published by the authoritative CI gate. */
    val authoritativeStatusName: Property<String> = project.objects.property(String::class.java)

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

    /** Adds one included build whose settings and local version registry must be verified. */
    fun versionedBuild(
        relativePath: String,
    ) {
        checkIncludedBuildVersions.configure { task ->
            task.includedBuildPaths.add(relativePath)
            task.includedBuildConfigurationFiles.from(
                project.layout.projectDirectory.file("$relativePath/settings.gradle.kts"),
                project.layout.projectDirectory.file("$relativePath/versions.properties"),
            )
        }
    }

    /**
     * Adds a reusable source scope and the implementation references it must not know.
     *
     * Only production Kotlin and Gradle configuration files are inspected.
     */
    fun reusableScope(
        relativePath: String,
        vararg forbiddenReferences: String,
    ) {
        checkModuleBoundaries.configure { task ->
            task.reusableScopePaths.add(relativePath)
            task.forbiddenReferencesByScope.put(
                relativePath,
                forbiddenReferences.joinToString(CheckModuleBoundariesTask.REFERENCE_SEPARATOR),
            )
            task.inspectedFiles.from(
                project.fileTree(project.layout.projectDirectory.dir(relativePath)) { files ->
                    files.include(
                        "settings.gradle.kts",
                        "**/*.gradle.kts",
                        "**/src/main/**/*.kt",
                    )
                    files.exclude(
                        "**/build/**",
                        "**/.gradle/**",
                        "**/tmp/**",
                    )
                },
            )
        }
    }

    /**
     * Exposes an included-build task through the root build without coupling the platform to its name.
     *
     * @return the root task provider created for the binding.
     */
    fun includedBuildTask(
        name: String,
        buildName: String,
        taskPath: String,
        description: String,
        requiredByCheck: Boolean = false,
        group: String = "verification",
    ): TaskProvider<Task> {
        val binding =
            project.tasks.register(name) { task ->
                task.group = group
                task.description = description
                task.dependsOn(project.gradle.includedBuild(buildName).task(taskPath))
            }
        if (requiredByCheck) {
            project.tasks.matching { task -> task.name == "check" }.configureEach { task ->
                task.dependsOn(binding)
            }
        }
        return binding
    }
}
