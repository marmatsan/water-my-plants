package com.marmatsan.verificationPlatform.plugin.task.ci

import com.marmatsan.verificationPlatform.data.git.GitRepositoryChangeSetSource
import com.marmatsan.verificationPlatform.data.json.CiPlanJson
import com.marmatsan.verificationPlatform.domain.model.ci.CiPlanPolicy
import com.marmatsan.verificationPlatform.domain.model.modules.ModuleDependency
import com.marmatsan.verificationPlatform.domain.model.modules.RepositoryModule
import com.marmatsan.verificationPlatform.domain.model.modules.RepositoryModuleGraph
import com.marmatsan.verificationPlatform.domain.service.ci.CiPlanFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/** Writes the provider-neutral CI plan for the committed repository change. */
@UntrackedTask(
    because = "The plan depends on Git revision state outside Gradle inputs"
)
abstract class GenerateCiPlanTask : DefaultTask() {
    /** Repository checkout whose committed Git state is classified. */
    @get:Internal
    abstract val repositoryRoot: DirectoryProperty

    /** Optional Git revision that replaces automatic comparison-base discovery. */
    @get:Optional
    @get:Input
    abstract val comparisonBaseOverride: Property<String>

    /** JSON file receiving the provider-neutral [com.marmatsan.verificationPlatform.domain.model.ci.CiPlan]. */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /** Gradle project paths mapped to normalized repository-relative directories. */
    @get:Input
    abstract val moduleDirectories: MapProperty<String, String>

    /** Directed module edges serialized as `dependent->dependency` strings. */
    @get:Input
    abstract val moduleDependencyEdges: ListProperty<String>

    /** Path prefixes classified as repository tooling by the consuming repository. */
    @get:Input
    abstract val toolingPathPrefixes: ListProperty<String>

    /** Path prefixes classified as build infrastructure by the consuming repository. */
    @get:Input
    abstract val buildInfrastructurePathPrefixes: ListProperty<String>

    /** Exact paths classified as build infrastructure by the consuming repository. */
    @get:Input
    abstract val buildInfrastructurePaths: ListProperty<String>

    /** Path prefixes that require staged portable-distribution verification. */
    @get:Input
    abstract val portableDistributionPathPrefixes: ListProperty<String>

    /** Exact paths that require staged portable-distribution verification. */
    @get:Input
    abstract val portableDistributionPaths: ListProperty<String>

    /** Agent capabilities required by repository tooling verification. */
    @get:Input
    abstract val toolingCapabilities: ListProperty<String>

    /** Agent capabilities required by build-infrastructure verification. */
    @get:Input
    abstract val buildInfrastructureCapabilities: ListProperty<String>

    /** Agent capabilities required by staged portable-distribution verification. */
    @get:Input
    abstract val portableDistributionCapabilities: ListProperty<String>

    /** Reviewed Gradle tasks selected for repository-tooling changes. */
    @get:Input
    abstract val toolingVerificationTasks: ListProperty<String>

    /** Reviewed Gradle tasks selected for build-infrastructure changes. */
    @get:Input
    abstract val buildInfrastructureVerificationTasks: ListProperty<String>

    /** Reviewed Gradle tasks selected for portable-distribution changes. */
    @get:Input
    abstract val portableDistributionVerificationTasks: ListProperty<String>

    /** Repository-wide tasks added to targeted module verification. */
    @get:Input
    abstract val targetedModuleSupplementalTasks: ListProperty<String>

    /** Reads committed changes, creates the plan, and writes [outputFile]. */
    @TaskAction
    fun generate() {
        val changeSet =
            GitRepositoryChangeSetSource().read(
                repositoryRoot = repositoryRoot.get().asFile,
                comparisonBaseOverride = comparisonBaseOverride.orNull
            )
        val moduleGraph =
            RepositoryModuleGraph(
                modules =
                    moduleDirectories.get().map { (id, directory) ->
                        RepositoryModule(
                            id = id,
                            directory = directory
                        )
                    },
                dependencies =
                    moduleDependencyEdges.get().map { edge ->
                        val parts =
                            edge.split(
                                EDGE_SEPARATOR,
                                limit = 2
                            )
                        check(parts.size == 2) { "Invalid serialized module dependency: $edge" }
                        ModuleDependency(
                            dependentModule = parts.first(),
                            dependencyModule = parts.last()
                        )
                    }
            )
        val plan =
            CiPlanFactory(
                CiPlanPolicy(
                    toolingPathPrefixes = toolingPathPrefixes.get(),
                    buildInfrastructurePathPrefixes = buildInfrastructurePathPrefixes.get(),
                    buildInfrastructurePaths = buildInfrastructurePaths.get().toSet(),
                    portableDistributionPathPrefixes = portableDistributionPathPrefixes.get(),
                    portableDistributionPaths = portableDistributionPaths.get().toSet(),
                    toolingCapabilities = toolingCapabilities.get(),
                    buildInfrastructureCapabilities = buildInfrastructureCapabilities.get(),
                    portableDistributionCapabilities = portableDistributionCapabilities.get(),
                    toolingVerificationTasks = toolingVerificationTasks.get(),
                    buildInfrastructureVerificationTasks = buildInfrastructureVerificationTasks.get(),
                    portableDistributionVerificationTasks = portableDistributionVerificationTasks.get(),
                    targetedModuleSupplementalTasks = targetedModuleSupplementalTasks.get()
                )
            ).create(
                changeSet,
                moduleGraph
            )
        val output = outputFile.get().asFile
        CiPlanJson().write(
            plan,
            output
        )

        logger.lifecycle(
            "CI plan generated: scope={}, fullVerification={}, output={}",
            plan.scope,
            plan.fullVerification,
            output.absolutePath
        )
    }

    private companion object {
        const val EDGE_SEPARATOR = "->"
    }
}
