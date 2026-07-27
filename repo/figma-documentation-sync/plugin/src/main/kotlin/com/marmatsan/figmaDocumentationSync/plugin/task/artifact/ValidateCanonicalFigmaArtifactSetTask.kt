package com.marmatsan.figmaDocumentationSync.plugin.task.artifact

import com.marmatsan.figmaDocumentationSync.plugin.di.create
import com.marmatsan.figmaDocumentationSync.plugin.di.figmaDocumentationSyncComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Validates a canonical artifact set and writes its typed handoff identity. */
@DisableCachingByDefault(
    because = "The output records absolute paths from the staged artifact set",
)
abstract class ValidateCanonicalFigmaArtifactSetTask : DefaultTask() {
    /** Directory containing the canonical model, scope, plan, manifests, and optional states. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val artifactDirectory: DirectoryProperty

    /** Optional repository revision that every artifact identity must match. */
    @get:Input
    @get:Optional
    abstract val expectedGitSha: Property<String>

    /** Typed JSON handoff written after the complete set passes validation. */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /** Validates artifact coherence and writes the resolved handoff paths and identity. */
    @TaskAction
    fun validateArtifactSet() {
        val component = figmaDocumentationSyncComponent::class.create()
        val artifacts =
            component.canonicalFigmaArtifactSetReader.read(
                artifactDirectory.get().asFile.absolutePath,
            )
        val validated =
            component.canonicalFigmaArtifactContractValidator.validate(
                contract = artifacts.contract,
                expectedGitSha = expectedGitSha.orNull,
            )
        val visualManifestPath =
            requireNotNull(artifacts.visualManifestPath) {
                "Canonical artifact set does not contain one visual manifest."
            }
        val metadataManifestPath =
            requireNotNull(artifacts.metadataManifestPath) {
                "Canonical artifact set does not contain one metadata manifest."
            }

        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(
            prettyJson.encodeToString(
                JsonObject.serializer(),
                JsonObject(
                    linkedMapOf(
                        "artifactDirectory" to JsonPrimitive(artifacts.artifactDirectory.toString()),
                        "gitSha" to JsonPrimitive(validated.gitSha),
                        "modelHash" to JsonPrimitive(validated.modelHash),
                        "decision" to JsonPrimitive(validated.decision.wireValue),
                        "modelPath" to JsonPrimitive(artifacts.modelPath.toString()),
                        "scopePath" to JsonPrimitive(artifacts.scopePath.toString()),
                        "planPath" to JsonPrimitive(artifacts.planPath.toString()),
                        "visualManifestPath" to JsonPrimitive(visualManifestPath.toString()),
                        "metadataManifestPath" to JsonPrimitive(metadataManifestPath.toString()),
                        "visualStatePath" to
                            JsonPrimitive(
                                visualManifestPath.parent
                                    .resolve(
                                        "execution-state.json",
                                    ).toString(),
                            ),
                    ),
                ),
            ) + System.lineSeparator(),
        )

        logger.lifecycle(
            "Validated canonical Figma artifact set at ${validated.gitSha} " +
                "(${validated.decision.wireValue}).",
        )
    }

    private companion object {
        val prettyJson = Json { prettyPrint = true }
    }
}
