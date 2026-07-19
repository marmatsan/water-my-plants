package com.marmatsan.figmaDocumentationSync.plugin.task.impact

import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaChangeImpact
import com.marmatsan.figmaDocumentationSync.domain.model.impact.RepositoryChangeSet
import com.marmatsan.figmaDocumentationSync.plugin.di.create
import com.marmatsan.figmaDocumentationSync.plugin.di.figmaDocumentationSyncComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Writes the deterministic Figma impact of the current Git change set. */
@DisableCachingByDefault(because = "The default input is the current Git revision graph")
abstract class ClassifyFigmaChangeImpactTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val policyFile: RegularFileProperty

    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    @get:Input
    abstract val changedPathsOverride: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val comparisonBaseOverride: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /** Reads the policy and changed paths, then writes the machine contract. */
    @TaskAction
    fun classify() {
        val component = figmaDocumentationSyncComponent::class.create()
        val policy = component.changeImpactPolicyPort.read(policyFile.get().asFile.absolutePath)
        val changeSet = changedPathsOverride.get().takeIf(List<String>::isNotEmpty)?.let { paths ->
            RepositoryChangeSet(
                comparisonBase = comparisonBaseOverride.orNull,
                changedPaths = paths
            )
        } ?: component.repositoryChangeSetPort.read(projectRootDirectory.get().asFile.absolutePath)
        val impact = component.changeImpactClassifier.classify(changeSet, policy)
        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(prettyJson.encodeToString(JsonObject.serializer(), impact.toJson()) + System.lineSeparator())

        logger.lifecycle(
            "Classified Figma change impact as ${impact.impact.wireValue} " +
                "(${impact.scope.wireValue})."
        )
    }

    private fun FigmaChangeImpact.toJson() = JsonObject(
        linkedMapOf(
            "scope" to JsonPrimitive(scope.wireValue),
            "figmaImpact" to JsonPrimitive(impact.wireValue),
            "affectedVisualTargets" to JsonArray(affectedVisualTargets.map(::JsonPrimitive)),
            "comparisonBase" to (comparisonBase?.let(::JsonPrimitive) ?: JsonNull),
            "changedPaths" to JsonArray(changedPaths.map(::JsonPrimitive))
        )
    )

    private companion object {
        val prettyJson = Json {
            prettyPrint = true
            explicitNulls = true
        }
    }
}
