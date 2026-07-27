package com.marmatsan.waterMyPlants.projectConfig

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.time.Clock

/** Creates the deterministic JSON handoff contract from already validated inputs. */
internal class TeamCityFigmaHandoffSummaryFactory(
    private val clock: Clock,
) {
    fun create(
        request: TeamCityFigmaSyncHandoffPreparer.Request,
        artifacts: CanonicalFigmaArtifactSet,
        validated: ValidatedCanonicalFigmaArtifact,
        inspection: CanonicalFigmaRunnerInspection,
    ): JsonObject {
        val visualManifest = requireNotNull(artifacts.visualManifestPath)
        val metadataManifest = requireNotNull(artifacts.metadataManifestPath)
        val nextUnit = inspection.executionFiles.firstOrNull() ?: "COMPLETE"
        val commandPrefix =
            ".\\gradlew.bat runFigmaMcp " +
                "-PfigmaMcpManifest=\"$visualManifest\" -PfigmaMcpPlan=\"${artifacts.planPath}\""
        return buildJsonObject {
            put(
                "schemaVersion",
                1,
            )
            put(
                "preparedAt",
                clock.instant().toString(),
            )
            putNullable(
                name = "teamCityBuildId",
                value = request.buildId?.let(::JsonPrimitive),
            )
            put(
                "gitSha",
                validated.gitSha,
            )
            put(
                "modelHash",
                validated.modelHash,
            )
            put(
                "decision",
                validated.decision.wireValue,
            )
            put(
                "nextUnit",
                nextUnit,
            )
            put(
                "artifactDirectory",
                artifacts.artifactDirectory.toString(),
            )
            put(
                "visualManifest",
                visualManifest.toString(),
            )
            put(
                "metadataManifest",
                metadataManifest.toString(),
            )
            put(
                "plan",
                artifacts.planPath.toString(),
            )
            put(
                "dryRun",
                inspection.toJson(),
            )
            put(
                "commands",
                buildJsonObject {
                    put(
                        "inspect",
                        "$commandPrefix -PfigmaMcpDryRun=true",
                    )
                    put(
                        "next",
                        "$commandPrefix -PfigmaMcpNext=true",
                    )
                    put(
                        "recordSuccess",
                        "$commandPrefix -PfigmaMcpRecordSuccess=\"RUNNER_FILE.mcp.js\" " +
                            "-PfigmaMcpSummary=\"SHORT_RESULT\"",
                    )
                    put(
                        "recordFailure",
                        "$commandPrefix -PfigmaMcpRecordFailure=\"RUNNER_FILE.mcp.js\" " +
                            "-PfigmaMcpSummary=\"SHORT_ERROR\"",
                    )
                    put(
                        "execute",
                        commandPrefix,
                    )
                    put(
                        "uploadPayload",
                        uploadPayloadCommand(
                            request = request,
                            artifacts = artifacts,
                            validated = validated,
                        ),
                    )
                    put(
                        "rerun",
                        ".\\gradlew.bat rerunTeamCityFigmaSync -PfigmaTeamCityWait=true",
                    )
                },
            )
        }
    }

    private fun CanonicalFigmaRunnerInspection.toJson(): JsonObject =
        buildJsonObject {
            put(
                "manifestHash",
                manifestHash,
            )
            put(
                "statePath",
                statePath,
            )
            put(
                "reuseStaging",
                reuseStaging,
            )
            putNullable(
                name = "decision",
                value = decision?.let(::JsonPrimitive),
            )
            put(
                "executionFiles",
                JsonArray(executionFiles.map(::JsonPrimitive)),
            )
        }

    private fun uploadPayloadCommand(
        request: TeamCityFigmaSyncHandoffPreparer.Request,
        artifacts: CanonicalFigmaArtifactSet,
        validated: ValidatedCanonicalFigmaArtifact,
    ): String =
        request.buildId?.let { buildId ->
            ".\\gradlew.bat uploadCanonicalFigmaPayload " +
                "-PfigmaTeamCityBuildId=$buildId " +
                "-PfigmaMcpUploadUrl=\"SINGLE_USE_UPLOAD_URL\""
        } ?: ".\\gradlew.bat uploadCanonicalFigmaPayload " +
            "-PfigmaArtifactDirectory=\"${artifacts.artifactDirectory}\" " +
            "-PfigmaExpectedGitSha=${validated.gitSha} " +
            "-PfigmaMcpUploadUrl=\"SINGLE_USE_UPLOAD_URL\""
}

/** Writes the handoff JSON beside the canonical artifact set. */
internal class JsonTeamCityFigmaHandoffSummaryWriter : TeamCityFigmaHandoffSummaryWriter {
    override fun write(
        artifactDirectory: File,
        summary: JsonObject,
    ): File {
        val summaryFile = artifactDirectory.resolve("figma-sync-handoff.json")
        summaryFile.writeText(
            prettyJson.encodeToString(
                JsonObject.serializer(),
                summary,
            ) + System.lineSeparator(),
        )
        return summaryFile
    }

    private companion object {
        val prettyJson = Json { prettyPrint = true }
    }
}

private fun JsonObjectBuilder.putNullable(
    name: String,
    value: JsonPrimitive?,
) {
    put(
        name,
        value ?: JsonNull,
    )
}
