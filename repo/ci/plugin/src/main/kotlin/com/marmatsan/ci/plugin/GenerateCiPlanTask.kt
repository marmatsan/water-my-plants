package com.marmatsan.ci.plugin

import com.marmatsan.ci.data.git.GitRepositoryChangeSetSource
import com.marmatsan.ci.data.json.CiPlanJson
import com.marmatsan.ci.domain.model.ModuleDependency
import com.marmatsan.ci.domain.model.RepositoryModule
import com.marmatsan.ci.domain.model.RepositoryModuleGraph
import com.marmatsan.ci.domain.service.CiPlanFactory
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
import org.gradle.work.DisableCachingByDefault

/** Writes the provider-neutral CI plan for the committed repository change. */
@DisableCachingByDefault(because = "The plan depends on Git revision state outside Gradle inputs")
abstract class GenerateCiPlanTask : DefaultTask() {
    @get:Internal
    abstract val repositoryRoot: DirectoryProperty

    @get:Optional
    @get:Input
    abstract val comparisonBaseOverride: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val moduleDirectories: MapProperty<String, String>

    @get:Input
    abstract val moduleDependencyEdges: ListProperty<String>

    @TaskAction
    fun generate() {
        val changeSet = GitRepositoryChangeSetSource().read(
            repositoryRoot = repositoryRoot.get().asFile,
            comparisonBaseOverride = comparisonBaseOverride.orNull
        )
        val moduleGraph = RepositoryModuleGraph(
            modules = moduleDirectories.get().map { (id, directory) ->
                RepositoryModule(id = id, directory = directory)
            },
            dependencies = moduleDependencyEdges.get().map { edge ->
                val parts = edge.split(EDGE_SEPARATOR, limit = 2)
                check(parts.size == 2) { "Invalid serialized module dependency: $edge" }
                ModuleDependency(
                    dependentModule = parts.first(),
                    dependencyModule = parts.last()
                )
            }
        )
        val plan = CiPlanFactory().create(changeSet, moduleGraph)
        val output = outputFile.get().asFile
        CiPlanJson().write(plan, output)

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
