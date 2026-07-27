package com.marmatsan.waterMyPlants.projectConfig.figma.handoff

import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model.CanonicalFigmaArtifactSet
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model.CanonicalFigmaRunnerInspection
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model.ValidatedCanonicalFigmaArtifact
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Clock

/** Creates the deterministic JSON handoff contract from already validated inputs. */
internal class TeamCityFigmaHandoffSummaryFactory(
    private val clock: Clock,
) {
    /** Creates an operator handoff from a fully [validated] artifact set and runner [inspection]. */
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

private fun JsonObjectBuilder.putNullable(
    name: String,
    value: JsonPrimitive?,
) {
    put(
        name,
        value ?: JsonNull,
    )
}
