package com.marmatsan.figmaDocumentationSync.projectConfig

import com.marmatsan.figmaDocumentationSync.data.figma.artifact.OfficialFigmaArtifactSetReader
import com.marmatsan.figmaDocumentationSync.data.mcp.McpRunnerExecutor
import com.marmatsan.figmaDocumentationSync.domain.service.artifact.OfficialFigmaArtifactContractValidator
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityBuildArtifactClient
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityCliClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Clock
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream

/** Prepares the validated local handoff for one official TeamCity Figma Sync artifact set. */
class TeamCityFigmaSyncHandoffPreparer(
    private val teamCityClient: TeamCityBuildArtifactClient = TeamCityCliClient(),
    private val artifactReader: OfficialFigmaArtifactSetReader = OfficialFigmaArtifactSetReader(),
    private val artifactValidator: OfficialFigmaArtifactContractValidator =
        OfficialFigmaArtifactContractValidator(),
    private val clock: Clock = Clock.systemUTC(),
    private val executor: McpRunnerExecutor = McpRunnerExecutor(),
) {
    fun prepare(
        request: Request,
    ): Result {
        val artifactDirectory =
            resolveArtifactDirectory(
                request = request,
            )
        val artifacts = artifactReader.read(artifactDirectory.absolutePath)
        val validated =
            artifactValidator.validate(
                contract = artifacts.contract,
                expectedGitSha = request.expectedGitSha,
            )
        val visualManifest =
            requireNotNull(artifacts.visualManifestPath) {
                "Official artifact set does not contain one visual manifest."
            }
        requireNotNull(artifacts.metadataManifestPath) {
            "Official artifact set does not contain one metadata manifest."
        }

        val inspection =
            executor.inspect(
                McpRunnerExecutor.Request(
                    manifestPath = visualManifest.toString(),
                    planPath = artifacts.planPath.toString(),
                ),
            )
        val dryRun =
            buildJsonObject {
                put(
                    "manifestHash",
                    inspection.manifestHash,
                )
                put(
                    "statePath",
                    inspection.statePath,
                )
                put(
                    "reuseStaging",
                    inspection.reuseStaging,
                )
                putNullable(
                    name = "decision",
                    value = inspection.decision?.let(::JsonPrimitive),
                )
                put(
                    "executionFiles",
                    JsonArray(
                        inspection.executionFiles.map(
                            transform = ::JsonPrimitive,
                        ),
                    ),
                )
            }
        val nextUnit = inspection.executionFiles.firstOrNull() ?: "COMPLETE"
        val commandPrefix =
            ".\\gradlew.bat runFigmaMcp " +
                "-PfigmaMcpManifest=\"$visualManifest\" -PfigmaMcpPlan=\"${artifacts.planPath}\""
        val uploadPayloadCommand =
            request.buildId?.let { buildId ->
                ".\\gradlew.bat uploadOfficialFigmaPayload " +
                    "-PfigmaTeamCityBuildId=$buildId " +
                    "-PfigmaMcpUploadUrl=\"SINGLE_USE_UPLOAD_URL\""
            } ?: ".\\gradlew.bat uploadOfficialFigmaPayload " +
                "-PfigmaArtifactDirectory=\"${artifacts.artifactDirectory}\" " +
                "-PfigmaExpectedGitSha=${validated.gitSha} " +
                "-PfigmaMcpUploadUrl=\"SINGLE_USE_UPLOAD_URL\""
        val summary =
            buildJsonObject {
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
                    artifacts.metadataManifestPath.toString(),
                )
                put(
                    "plan",
                    artifacts.planPath.toString(),
                )
                put(
                    "dryRun",
                    dryRun,
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
                            uploadPayloadCommand,
                        )
                        put(
                            "rerun",
                            ".\\gradlew.bat rerunTeamCityFigmaSync -PfigmaTeamCityWait=true",
                        )
                    },
                )
            }
        val summaryFile =
            artifacts.artifactDirectory
                .resolve(
                    "figma-sync-handoff.json",
                ).toFile()
        summaryFile.writeText(
            prettyJson.encodeToString(
                JsonObject.serializer(),
                summary,
            ) + System.lineSeparator(),
        )

        return Result(
            artifactDirectory = artifactDirectory,
            summaryFile = summaryFile,
            summary = summary,
        )
    }

    private fun resolveArtifactDirectory(
        request: Request,
    ): File {
        require((request.buildId == null) xor (request.artifactDirectory == null)) {
            "Configure exactly one of figmaTeamCityBuildId or figmaArtifactDirectory."
        }
        request.artifactDirectory?.let { directory ->
            require(directory.isDirectory) { "Artifact directory does not exist: ${directory.path}" }
            return directory
                .toPath()
                .toAbsolutePath()
                .normalize()
                .toFile()
        }

        val buildId = requireNotNull(request.buildId)
        val build =
            teamCityClient.readBuild(
                buildId = buildId,
            )
        require(build.id == buildId) {
            "TeamCity returned build ${build.id} while build $buildId was requested."
        }
        require(build.state == "finished" && build.status == "SUCCESS") {
            "Build $buildId must be finished and successful; found state '${build.state}' " +
                "and status '${build.status}'."
        }
        require(build.branchName in request.mainBranchAliases) {
            "Build $buildId is not from main; found branch '${build.branchName}'."
        }
        require(build.buildTypeName.contains(request.requiredBuildTypeName)) {
            "Build $buildId is '${build.buildTypeName}', not the ${request.requiredBuildTypeName} job."
        }

        val timestamp =
            downloadTimestamp.format(
                clock.instant(),
            )
        val output =
            request.destinationRoot.resolve(
                relative = "figma-sync-$buildId-$timestamp",
            )
        require(output.mkdirs()) { "Could not create TeamCity artifact directory: ${output.path}" }
        teamCityClient.downloadArtifacts(
            buildId = buildId,
            outputDirectory = output,
        )
        expandSharedArchiveWhenNeeded(
            directory = output,
        )
        return output
            .toPath()
            .toAbsolutePath()
            .normalize()
            .toFile()
    }

    private fun expandSharedArchiveWhenNeeded(
        directory: File,
    ) {
        val models =
            directory.findFiles(
                fileName = "design-model.json",
            )
        val archives =
            directory.findFiles(
                fileName = ".shared_files.zip",
            )
        if (models.isEmpty() && archives.size == 1) {
            expandZip(
                archive = archives.single(),
                destination =
                    directory.resolve(
                        relative = "shared-files",
                    ),
            )
        } else {
            require(archives.size <= 1) {
                "TeamCity returned more than one .shared_files.zip artifact."
            }
        }
    }

    private fun expandZip(
        archive: File,
        destination: File,
    ) {
        val root = destination.toPath().toAbsolutePath().normalize()
        Files.createDirectories(root)
        ZipInputStream(archive.inputStream().buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val target =
                    root
                        .resolve(
                            entry.name,
                        ).normalize()
                require(
                    target.startsWith(
                        root,
                    ),
                ) { "Unsafe ZIP entry '${entry.name}'." }
                if (entry.isDirectory) {
                    Files.createDirectories(target)
                } else {
                    Files.createDirectories(target.parent)
                    Files.copy(
                        zip,
                        target,
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    data class Request(
        val buildId: Long?,
        val artifactDirectory: File?,
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

    data class Result(
        val artifactDirectory: File,
        val summaryFile: File,
        val summary: JsonObject,
    )

    private companion object {
        val prettyJson = Json { prettyPrint = true }
        val downloadTimestamp: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC)
    }
}

private fun File.findFiles(
    fileName: String,
): List<File> =
    Files.walk(toPath()).use { paths ->
        paths
            .filter { path -> Files.isRegularFile(path) && path.fileName.toString() == fileName }
            .map(
                java.nio.file.Path::toFile,
            ).toList()
    }

private fun kotlinx.serialization.json.JsonObjectBuilder.putNullable(
    name: String,
    value: JsonPrimitive?,
) {
    put(
        name,
        value ?: JsonNull,
    )
}
