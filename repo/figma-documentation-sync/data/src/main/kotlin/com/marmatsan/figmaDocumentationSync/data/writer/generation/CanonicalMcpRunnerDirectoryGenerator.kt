package com.marmatsan.figmaDocumentationSync.data.writer.generation

import com.marmatsan.figmaDocumentationSync.data.fingerprint.WriterScopeFingerprintCalculator
import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.writer.ExecutableRunnerManifestJson
import com.marmatsan.figmaDocumentationSync.data.writer.CanonicalMcpRunnerGenerator
import com.marmatsan.figmaDocumentationSync.domain.model.writer.ExecutableRunnerManifest
import java.nio.charset.StandardCharsets
import java.nio.file.Path

/** Generates one executable canonical runner directory and its finalized manifest. */
internal class CanonicalMcpRunnerDirectoryGenerator(
    private val stagingPlanner: CanonicalMcpRunnerStagingPlanner,
    private val targetPlanner: CanonicalMcpRunnerTargetPlanner,
    private val manifestJson: ExecutableRunnerManifestJson,
    private val directoryStore: CanonicalMcpRunnerDirectoryStore = CanonicalMcpRunnerDirectoryStore()
) {
    /** Recreates and generates one runner for [targets]. */
    fun generate(
        context: CanonicalMcpRunnerGenerationContext,
        outputDirectory: Path,
        targets: List<String>,
        writeMetadata: Boolean,
        fullVisualSync: Boolean
    ): ExecutableRunnerManifest {
        directoryStore.recreate(outputDirectory)
        val sources = linkedMapOf<String, String>()
        val payloadImage =
            stagingPlanner.stage(
                context = context,
                outputDirectory = outputDirectory,
                sources = sources
            )
        val executionScopes =
            targetPlanner.addTargetSources(
                sources = sources,
                context = context,
                targets = targets,
                writeMetadata = writeMetadata,
                fullVisualSync = fullVisualSync
            )
        directoryStore.writeSources(
            directory = outputDirectory,
            sources = sources
        )
        val fileHashes =
            sources.mapValues { (_, source) ->
                Sha256Hash.of(source.toByteArray(StandardCharsets.UTF_8))
            }
        val manifestDraft =
            ExecutableRunnerManifest(
                path = outputDirectory.resolve(CanonicalMcpRunnerGenerationContract.MANIFEST_FILE).toString(),
                schemaVersion = CanonicalMcpRunnerGenerator.MANIFEST_SCHEMA_VERSION,
                mode = "canonical",
                entrypoint = "trunk-sync",
                target = targets.first(),
                targets = targets,
                writeMetadata = writeMetadata,
                transport = context.request.transport,
                namespace = context.request.config.canonicalStagingNamespace,
                sectionNodeId = null,
                roots = emptyList(),
                allowCanonicalSections = false,
                fullVisualSync = fullVisualSync,
                allowPartial = false,
                metadataPageId = context.request.config.metadataPageId,
                modelPath =
                    directoryStore.portablePath(
                        toolsDirectory = context.request.toolsDirectory,
                        path = context.request.modelPath
                    ),
                scriptPath =
                    directoryStore.portablePath(
                        toolsDirectory = context.request.toolsDirectory,
                        path = context.request.scriptPath
                    ),
                modelHash = context.modelHash,
                gitSha = context.gitSha,
                designModelLength = context.modelJson.length,
                scriptLength = context.script.length,
                writerHash = context.writerHash,
                transportHash = context.transportHash,
                targetFingerprints = context.targetFingerprints,
                writerScopeFingerprints = context.writerScopeFingerprints,
                writerScopeFingerprintSchemaVersion = WriterScopeFingerprintCalculator.SCHEMA_VERSION,
                executionScopes = executionScopes,
                payloadImage = payloadImage,
                files = sources.keys.toList(),
                fileHashes = fileHashes,
                manifestHash = ""
            )
        return manifestJson.finalizeAndWrite(
            draft = manifestDraft,
            outputPath = outputDirectory.resolve(CanonicalMcpRunnerGenerationContract.MANIFEST_FILE).toString()
        )
    }
}
