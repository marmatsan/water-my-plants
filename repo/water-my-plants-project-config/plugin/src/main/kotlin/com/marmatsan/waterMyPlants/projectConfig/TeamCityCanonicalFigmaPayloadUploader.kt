package com.marmatsan.waterMyPlants.projectConfig

import com.marmatsan.figmaDocumentationSync.data.figma.artifact.CanonicalFigmaArtifactSetReader
import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.writer.ExecutableRunnerManifestJson
import com.marmatsan.figmaDocumentationSync.data.mcp.KtorFigmaPngAssetUploader
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.TeamCityFigmaSyncHandoffPreparer
import java.io.File
import java.nio.file.Files

/** Uploads the verified PNG from one canonical main artifact set. */
class TeamCityCanonicalFigmaPayloadUploader(
    private val handoffPreparer: TeamCityFigmaSyncHandoffPreparer =
        TeamCityFigmaSyncHandoffPreparer(),
    private val artifactReader: CanonicalFigmaArtifactSetReader = CanonicalFigmaArtifactSetReader(),
    private val manifestJson: ExecutableRunnerManifestJson = ExecutableRunnerManifestJson(),
    private val uploadPng: (String, ByteArray) -> Unit =
        KtorFigmaPngAssetUploader()::uploadBlocking,
) {
    /** Prepares and verifies one canonical artifact set before uploading its exact PNG payload. */
    fun upload(
        request: Request,
    ): Result {
        require((request.buildId == null) xor (request.artifactDirectory == null)) {
            "Configure exactly one of figmaTeamCityBuildId or figmaArtifactDirectory."
        }
        if (request.artifactDirectory != null) {
            require(!request.expectedGitSha.isNullOrBlank()) {
                "figmaExpectedGitSha is required with figmaArtifactDirectory."
            }
        }
        val handoff =
            handoffPreparer.prepare(
                request =
                    TeamCityFigmaSyncHandoffPreparer.Request(
                        buildId = request.buildId,
                        artifactDirectory = request.artifactDirectory,
                        destinationRoot = request.destinationRoot,
                        expectedGitSha = request.expectedGitSha,
                        mainBranchAliases = request.mainBranchAliases,
                        requiredBuildTypeName = request.requiredBuildTypeName,
                    ),
            )
        val artifacts = artifactReader.read(handoff.artifactDirectory.absolutePath)
        val manifestPath =
            requireNotNull(artifacts.visualManifestPath) {
                "Canonical artifact set does not contain one visual manifest."
            }
        val manifest = manifestJson.read(manifestPath.toString())
        require(manifest.mode == "canonical" && manifest.fullVisualSync && !manifest.writeMetadata) {
            "Figma payload upload requires the canonical full visual manifest."
        }
        require(manifest.transport == PNG_TRANSPORT) {
            "Figma payload upload requires PNG transport; found '${manifest.transport}'."
        }
        val payload =
            requireNotNull(manifest.payloadImage) {
                "Canonical visual manifest does not declare a PNG payload."
            }
        require(File(payload.fileName).name == payload.fileName) {
            "Canonical PNG payload must use a file name without path segments."
        }
        val runnerDirectory = requireNotNull(manifestPath.parent).toAbsolutePath().normalize()
        val payloadPath =
            runnerDirectory
                .resolve(
                    payload.fileName,
                ).normalize()
        require(payloadPath.parent == runnerDirectory && Files.isRegularFile(payloadPath)) {
            "Canonical PNG payload does not exist beside its visual manifest."
        }
        val bytes = Files.readAllBytes(payloadPath)
        require(bytes.size == payload.byteLength) {
            "Canonical PNG payload length mismatch: ${bytes.size} != ${payload.byteLength}."
        }
        val actualHash =
            Sha256Hash.of(
                value = bytes,
            )
        require(actualHash == payload.sha256) {
            "Canonical PNG payload hash mismatch: $actualHash != ${payload.sha256}."
        }

        uploadPng(
            request.uploadUrl,
            bytes,
        )
        return Result(
            buildId = request.buildId,
            gitSha = manifest.gitSha,
            modelHash = manifest.modelHash,
            payloadFileName = payload.fileName,
            payloadByteLength = payload.byteLength,
            payloadSha256 = payload.sha256,
            artifactDirectory = handoff.artifactDirectory,
        )
    }

    /**
     * Inputs that identify and constrain the canonical payload being uploaded.
     *
     * @property buildId TeamCity build to download, mutually exclusive with [artifactDirectory].
     * @property artifactDirectory existing local artifact set, mutually exclusive with [buildId].
     * @property uploadUrl single-use Figma MCP PNG destination.
     * @property destinationRoot safe root for downloaded and extracted artifacts.
     * @property expectedGitSha required repository revision for local artifacts.
     * @property mainBranchAliases accepted TeamCity spellings of the canonical branch.
     * @property requiredBuildTypeName build configuration allowed to supply production artifacts.
     */
    data class Request(
        val buildId: Long?,
        val artifactDirectory: File?,
        val uploadUrl: String,
        val destinationRoot: File,
        val expectedGitSha: String? = null,
        val mainBranchAliases: Set<String> =
            setOf(
                "main",
                "<default>",
                "refs/heads/main",
            ),
        val requiredBuildTypeName: String = "Generate main design model",
    )

    /**
     * Verified identity of the PNG payload accepted for upload.
     *
     * @property buildId source TeamCity build when artifacts were downloaded.
     * @property gitSha repository revision declared by the canonical manifest.
     * @property modelHash canonical design-model hash declared by the manifest.
     * @property payloadFileName verified PNG file name.
     * @property payloadByteLength verified PNG byte length.
     * @property payloadSha256 verified PNG content hash.
     * @property artifactDirectory prepared artifact set containing the uploaded payload.
     */
    data class Result(
        val buildId: Long?,
        val gitSha: String,
        val modelHash: String,
        val payloadFileName: String,
        val payloadByteLength: Int,
        val payloadSha256: String,
        val artifactDirectory: File,
    )

    private companion object {
        const val PNG_TRANSPORT = "png"
    }
}
