package com.marmatsan.waterMyPlants.projectConfig

import com.marmatsan.figmaDocumentationSync.data.figma.artifact.CanonicalFigmaArtifactSetReader
import com.marmatsan.figmaDocumentationSync.data.mcp.McpRunnerExecutor
import com.marmatsan.figmaDocumentationSync.domain.model.artifact.CanonicalFigmaArtifactContract
import com.marmatsan.figmaDocumentationSync.domain.service.artifact.CanonicalFigmaArtifactContractValidator
import kotlinx.serialization.json.JsonObject
import java.io.File
import java.nio.file.Path

/** Supplies the canonical artifact data required by the handoff use case. */
internal fun interface CanonicalFigmaArtifactSetSource {
    fun read(
        artifactDirectory: File,
    ): CanonicalFigmaArtifactSet
}

/** Verifies the canonical identity without exposing a concrete validator to the use case. */
internal fun interface CanonicalFigmaArtifactVerifier {
    fun verify(
        contract: CanonicalFigmaArtifactContract,
        expectedGitSha: String?,
    ): ValidatedCanonicalFigmaArtifact
}

/** Supplies the deterministic runner inspection required to create the handoff. */
internal fun interface CanonicalFigmaRunnerInspector {
    fun inspect(
        manifestPath: Path,
        planPath: Path,
    ): CanonicalFigmaRunnerInspection
}

/** Persists the generated handoff summary. */
internal fun interface TeamCityFigmaHandoffSummaryWriter {
    fun write(
        artifactDirectory: File,
        summary: JsonObject,
    ): File
}

internal data class CanonicalFigmaArtifactSet(
    val artifactDirectory: Path,
    val planPath: Path,
    val visualManifestPath: Path?,
    val metadataManifestPath: Path?,
    val contract: CanonicalFigmaArtifactContract,
)

internal data class ValidatedCanonicalFigmaArtifact(
    val gitSha: String,
    val modelHash: String,
    val decision: CanonicalFigmaArtifactContract.Decision,
)

internal data class CanonicalFigmaRunnerInspection(
    val manifestHash: String,
    val statePath: String,
    val reuseStaging: Boolean,
    val decision: String?,
    val executionFiles: List<String>,
)

internal class DefaultCanonicalFigmaArtifactSetSource(
    private val reader: CanonicalFigmaArtifactSetReader = CanonicalFigmaArtifactSetReader(),
) : CanonicalFigmaArtifactSetSource {
    override fun read(
        artifactDirectory: File,
    ): CanonicalFigmaArtifactSet {
        val result = reader.read(artifactDirectory.absolutePath)
        return CanonicalFigmaArtifactSet(
            artifactDirectory = result.artifactDirectory,
            planPath = result.planPath,
            visualManifestPath = result.visualManifestPath,
            metadataManifestPath = result.metadataManifestPath,
            contract = result.contract,
        )
    }
}

internal class DefaultCanonicalFigmaArtifactVerifier(
    private val validator: CanonicalFigmaArtifactContractValidator = CanonicalFigmaArtifactContractValidator(),
) : CanonicalFigmaArtifactVerifier {
    override fun verify(
        contract: CanonicalFigmaArtifactContract,
        expectedGitSha: String?,
    ): ValidatedCanonicalFigmaArtifact {
        val result =
            validator.validate(
                contract,
                expectedGitSha,
            )
        return ValidatedCanonicalFigmaArtifact(
            gitSha = result.gitSha,
            modelHash = result.modelHash,
            decision = result.decision,
        )
    }
}

internal class DefaultCanonicalFigmaRunnerInspector(
    private val executor: McpRunnerExecutor = McpRunnerExecutor(),
) : CanonicalFigmaRunnerInspector {
    override fun inspect(
        manifestPath: Path,
        planPath: Path,
    ): CanonicalFigmaRunnerInspection {
        val inspection =
            executor.inspect(
                McpRunnerExecutor.Request(
                    manifestPath = manifestPath.toString(),
                    planPath = planPath.toString(),
                ),
            )
        return CanonicalFigmaRunnerInspection(
            manifestHash = inspection.manifestHash,
            statePath = inspection.statePath,
            reuseStaging = inspection.reuseStaging,
            decision = inspection.decision,
            executionFiles = inspection.executionFiles,
        )
    }
}
