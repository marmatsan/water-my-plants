package com.marmatsan.figmaDesignSync.projectConfig

import com.marmatsan.figmaDesignSync.data.figma.artifact.OfficialFigmaArtifactSetReader
import com.marmatsan.figmaDesignSync.domain.service.artifact.OfficialFigmaArtifactContractValidator
import com.marmatsan.figmaDesignSync.teamcityAdapter.TeamCityBuildArtifactClient
import com.marmatsan.figmaDesignSync.teamcityAdapter.TeamCityCliClient
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Clock
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Prepares the validated local handoff for one official TeamCity Figma Sync artifact set. */
class TeamCityFigmaSyncHandoffPreparer(
    private val teamCityClient: TeamCityBuildArtifactClient = TeamCityCliClient(),
    private val artifactReader: OfficialFigmaArtifactSetReader = OfficialFigmaArtifactSetReader(),
    private val artifactValidator: OfficialFigmaArtifactContractValidator =
        OfficialFigmaArtifactContractValidator(),
    private val clock: Clock = Clock.systemUTC(),
    private val execute: (File, List<String>) -> CommandResult = { directory, arguments ->
        executeProcess(directory, arguments)
    }
) {
    fun prepare(request: Request): Result {
        val artifactDirectory = resolveArtifactDirectory(request)
        val artifacts = artifactReader.read(artifactDirectory.absolutePath)
        val validated = artifactValidator.validate(
            contract = artifacts.contract,
            expectedGitSha = request.expectedGitSha
        )
        val visualManifest = requireNotNull(artifacts.visualManifestPath) {
            "Official artifact set does not contain one visual manifest."
        }
        requireNotNull(artifacts.metadataManifestPath) {
            "Official artifact set does not contain one metadata manifest."
        }

        val executor = request.toolsDirectory.resolve("dist/execute-mcp-runner.mjs")
        if (!request.skipExecutorBuild) {
            run(request.toolsDirectory, npmExecutable(), "ci")
            run(request.toolsDirectory, npmExecutable(), "run", "build")
        }

        val dryRun: String?
        val nextUnit: String?
        if (executor.isFile) {
            val arguments = listOf(
                "node",
                executor.absolutePath,
                "--manifest=${visualManifest}",
                "--plan=${artifacts.planPath}"
            )
            dryRun = capture(request.projectRootDirectory, arguments + "--dry-run")
            nextUnit = capture(request.projectRootDirectory, arguments + "--next")
        } else {
            require(request.skipExecutorBuild) { "Missing built Figma executor: ${executor.path}" }
            dryRun = null
            nextUnit = null
        }

        val commandPrefix =
            "node \"${executor.absolutePath}\" --manifest=\"$visualManifest\" --plan=\"${artifacts.planPath}\""
        val summary = buildJsonObject {
            put("schemaVersion", 1)
            put("preparedAt", clock.instant().toString())
            putNullable("teamCityBuildId", request.buildId?.let(::JsonPrimitive))
            put("gitSha", validated.gitSha)
            put("modelHash", validated.modelHash)
            put("decision", validated.decision.wireValue)
            putNullable("nextUnit", nextUnit?.let(::JsonPrimitive))
            put("artifactDirectory", artifacts.artifactDirectory.toString())
            put("visualManifest", visualManifest.toString())
            put("metadataManifest", artifacts.metadataManifestPath.toString())
            put("plan", artifacts.planPath.toString())
            putNullable("dryRun", dryRun?.let(::JsonPrimitive))
            put(
                "commands",
                buildJsonObject {
                    put("inspect", "$commandPrefix --dry-run")
                    put("next", "$commandPrefix --next")
                    put(
                        "recordSuccess",
                        "$commandPrefix --record-success=\"RUNNER_FILE.mcp.js\" --summary=\"SHORT_RESULT\""
                    )
                    put(
                        "recordFailure",
                        "$commandPrefix --record-failure=\"RUNNER_FILE.mcp.js\" --summary=\"SHORT_ERROR\""
                    )
                    put("rerun", ".\\gradlew.bat rerunTeamCityFigmaSync -PfigmaTeamCityWait=true")
                }
            )
        }
        val summaryFile = artifacts.artifactDirectory.resolve("figma-sync-handoff.json").toFile()
        summaryFile.writeText(
            prettyJson.encodeToString(JsonObject.serializer(), summary) + System.lineSeparator()
        )

        return Result(
            artifactDirectory = artifactDirectory,
            summaryFile = summaryFile,
            summary = summary
        )
    }

    private fun resolveArtifactDirectory(request: Request): File {
        require((request.buildId == null) xor (request.artifactDirectory == null)) {
            "Configure exactly one of figmaTeamCityBuildId or figmaArtifactDirectory."
        }
        request.artifactDirectory?.let { directory ->
            require(directory.isDirectory) { "Artifact directory does not exist: ${directory.path}" }
            return directory.toPath().toAbsolutePath().normalize().toFile()
        }

        val buildId = requireNotNull(request.buildId)
        val build = teamCityClient.readBuild(buildId)
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

        val timestamp = downloadTimestamp.format(clock.instant())
        val output = request.destinationRoot.resolve("figma-sync-$buildId-$timestamp")
        require(output.mkdirs()) { "Could not create TeamCity artifact directory: ${output.path}" }
        teamCityClient.downloadArtifacts(buildId, output)
        expandSharedArchiveWhenNeeded(output)
        return output.toPath().toAbsolutePath().normalize().toFile()
    }

    private fun expandSharedArchiveWhenNeeded(directory: File) {
        val models = directory.findFiles("design-model.json")
        val archives = directory.findFiles(".shared_files.zip")
        if (models.isEmpty() && archives.size == 1) {
            expandZip(archives.single(), directory.resolve("shared-files"))
        } else {
            require(archives.size <= 1) {
                "TeamCity returned more than one .shared_files.zip artifact."
            }
        }
    }

    private fun expandZip(archive: File, destination: File) {
        val root = destination.toPath().toAbsolutePath().normalize()
        Files.createDirectories(root)
        ZipInputStream(archive.inputStream().buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val target = root.resolve(entry.name).normalize()
                require(target.startsWith(root)) { "Unsafe ZIP entry '${entry.name}'." }
                if (entry.isDirectory) {
                    Files.createDirectories(target)
                } else {
                    Files.createDirectories(target.parent)
                    Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    private fun run(directory: File, vararg arguments: String) {
        val result = execute(directory, platformCommand(arguments.toList()))
        require(result.exitCode == 0) {
            "Command '${arguments.joinToString(" ")}' failed with exit code ${result.exitCode}: " +
                result.error.trim()
        }
    }

    private fun capture(directory: File, arguments: List<String>): String {
        val result = execute(directory, platformCommand(arguments))
        require(result.exitCode == 0) {
            "Command '${arguments.joinToString(" ")}' failed with exit code ${result.exitCode}: " +
                result.error.trim()
        }
        return result.output.trim()
    }

    private fun platformCommand(arguments: List<String>): List<String> =
        if (isWindows() && arguments.first().endsWith(".cmd", ignoreCase = true)) {
            listOf("cmd.exe", "/d", "/c") + arguments
        } else {
            arguments
        }

    private fun npmExecutable(): String = if (isWindows()) "npm.cmd" else "npm"

    private fun isWindows(): Boolean =
        System.getProperty("os.name").startsWith("Windows", ignoreCase = true)

    data class Request(
        val buildId: Long?,
        val artifactDirectory: File?,
        val destinationRoot: File,
        val projectRootDirectory: File,
        val toolsDirectory: File,
        val skipExecutorBuild: Boolean,
        val expectedGitSha: String? = null,
        val mainBranchAliases: Set<String> = setOf("main", "<default>", "refs/heads/main"),
        val requiredBuildTypeName: String = "Generate main design model"
    )

    data class Result(
        val artifactDirectory: File,
        val summaryFile: File,
        val summary: JsonObject
    )

    data class CommandResult(
        val exitCode: Int,
        val output: String,
        val error: String
    )

    private companion object {
        val prettyJson = Json { prettyPrint = true }
        val downloadTimestamp: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC)

        fun executeProcess(directory: File, arguments: List<String>): CommandResult {
            val process = ProcessBuilder(arguments).directory(directory).start()
            val output = ByteArrayOutputStream()
            val error = ByteArrayOutputStream()
            process.inputStream.use { input -> input.copyTo(output) }
            process.errorStream.use { input -> input.copyTo(error) }
            return CommandResult(
                exitCode = process.waitFor(),
                output = output.toString(),
                error = error.toString()
            )
        }
    }
}

private fun File.findFiles(fileName: String): List<File> =
    Files.walk(toPath()).use { paths ->
        paths.filter { path -> Files.isRegularFile(path) && path.fileName.toString() == fileName }
            .map(java.nio.file.Path::toFile)
            .toList()
    }

private fun kotlinx.serialization.json.JsonObjectBuilder.putNullable(
    name: String,
    value: JsonPrimitive?
) {
    put(name, value ?: JsonNull)
}
