package com.marmatsan.figmaDocumentationSync.plugin.task.official

import com.marmatsan.figmaDocumentationSync.data.figma.client.FigmaFileContentClient
import com.marmatsan.figmaDocumentationSync.data.figma.common.FigmaNodeUrl
import com.marmatsan.figmaDocumentationSync.data.json.writer.FigmaSyncMetadataJson
import com.marmatsan.figmaDocumentationSync.data.json.writer.FigmaWriterRuntimeConfigJson
import com.marmatsan.figmaDocumentationSync.data.json.writer.VisualSyncPlanJson
import com.marmatsan.figmaDocumentationSync.data.writer.OfficialMcpRunnerGenerator
import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaVerificationScope
import com.marmatsan.figmaDocumentationSync.domain.model.sync.OfficialFigmaSyncScope
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaSyncMetadata
import com.marmatsan.figmaDocumentationSync.domain.service.writer.VisualSyncPlanner
import com.marmatsan.figmaDocumentationSync.plugin.di.create
import com.marmatsan.figmaDocumentationSync.plugin.di.figmaDocumentationSyncComponent
import java.io.ByteArrayOutputStream
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Builds the official MCP runner artifacts and writes their shared sync scope. */
@DisableCachingByDefault(because = "Builds the TypeScript Figma boundary and generates official runner artifacts")
abstract class PrepareOfficialFigmaSyncTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val changeImpactFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val changeImpactPolicyFile: RegularFileProperty

    @get:Internal
    abstract val designModelFile: RegularFileProperty

    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val metadataNodeUrl: Property<String>

    @get:Input
    @get:Optional
    abstract val metadataNamespace: Property<String>

    @get:Internal
    abstract val toolsDirectory: DirectoryProperty

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val writerProjectConfigFile: RegularFileProperty

    @get:OutputDirectory
    abstract val runnerOutputDirectory: DirectoryProperty

    @get:Internal
    abstract val visualSyncPlanFile: RegularFileProperty

    @get:OutputFile
    abstract val scopeFile: RegularFileProperty

    @get:Input
    abstract val runnerTransport: Property<String>

    @get:Input
    abstract val runnerChunkSize: Property<Int>

    /** Runs the writer toolchain only for a full verification, then writes `sync-scope.json`. */
    @TaskAction
    fun prepare() {
        val component = figmaDocumentationSyncComponent::class.create()
        val scopeJson = component.officialFigmaSyncScopeJson
        val impact = scopeJson.readChangeImpact(changeImpactFile.get().asFile.absolutePath)
        val gitSha = capture(projectRootDirectory.get().asFile, "git", "rev-parse", "HEAD")

        val scope = if (impact.scope == FigmaVerificationScope.FULL_VERIFICATION) {
            val model = designModelFile.get().asFile
            if (!model.isFile) {
                throw GradleException("Missing official Figma design model artifact: ${model.path}")
            }

            val tools = toolsDirectory.get().asFile
            val projectConfig = writerProjectConfigFile.orNull?.asFile
                ?: throw GradleException("Official MCP runner generation requires writerProjectConfigFile.")
            run(tools, npmExecutable(), "ci")
            buildWriter(tools, projectConfig)

            val runnerDirectory = runnerOutputDirectory.get().asFile
            val writerScript = tools.resolve("sync-trunk-design-model.mcp.js")
            if (!writerScript.isFile) {
                throw GradleException("Missing compiled Figma writer: ${writerScript.path}")
            }
            OfficialMcpRunnerGenerator().generate(
                OfficialMcpRunnerGenerator.Request(
                    modelPath = model.absolutePath,
                    scriptPath = writerScript.absolutePath,
                    outputDirectory = runnerDirectory.absolutePath,
                    toolsDirectory = tools.absolutePath,
                    writerSourceDirectory = tools.resolve("src").absolutePath,
                    repositoryRootDirectory = projectRootDirectory.get().asFile.absolutePath,
                    changeImpactPolicyPath = changeImpactPolicyFile.get().asFile.absolutePath,
                    config = FigmaWriterRuntimeConfigJson.read(projectConfig.absolutePath),
                    transport = runnerTransport.get(),
                    chunkSize = runnerChunkSize.get()
                )
            )

            val manifests = scopeJson.readRunnerManifests(runnerDirectory.absolutePath)
            val visualManifest = manifests.singleOrNull { manifest -> manifest.fullVisualSync }
                ?: throw GradleException("Official visual MCP runner manifest was not generated exactly once.")
            val metadataManifest = manifests.singleOrNull { manifest -> manifest.writeMetadata }
                ?: throw GradleException("Official metadata MCP runner manifest was not generated exactly once.")
            val plan = visualSyncPlanFile.get().asFile
            val planJson = VisualSyncPlanJson()
            val visualPlan = VisualSyncPlanner(planJson).create(visualManifest, readPreviousMetadata())
            planJson.write(visualPlan, plan.absolutePath)

            OfficialFigmaSyncScope(
                scope = impact.scope,
                figmaImpact = impact.impact,
                affectedVisualTargets = impact.affectedVisualTargets,
                comparisonBase = impact.comparisonBase,
                gitSha = gitSha,
                modelHash = visualManifest.modelHash,
                writerHash = visualManifest.writerHash,
                transportHash = visualManifest.transportHash,
                targetFingerprints = visualManifest.targetFingerprints,
                writerScopeFingerprints = visualManifest.writerScopeFingerprints,
                writerScopeFingerprintSchemaVersion = visualManifest.writerScopeFingerprintSchemaVersion,
                visualRunnerManifestHash = visualManifest.manifestHash,
                metadataRunnerManifestHash = metadataManifest.manifestHash,
                visualSyncDecision = visualPlan.body.decision.wireValue,
                visualSyncPlanHash = visualPlan.planHash
            )
        } else {
            OfficialFigmaSyncScope(
                scope = impact.scope,
                figmaImpact = impact.impact,
                affectedVisualTargets = impact.affectedVisualTargets,
                comparisonBase = impact.comparisonBase,
                gitSha = gitSha,
                modelHash = null,
                writerHash = null,
                transportHash = null,
                targetFingerprints = null,
                writerScopeFingerprints = null,
                writerScopeFingerprintSchemaVersion = null,
                visualRunnerManifestHash = null,
                metadataRunnerManifestHash = null,
                visualSyncDecision = null,
                visualSyncPlanHash = null
            )
        }

        scopeJson.write(scope, scopeFile.get().asFile.absolutePath)
        logger.lifecycle("Prepared official Figma Sync scope: ${scope.scope.wireValue}")
    }

    private fun readPreviousMetadata(): FigmaSyncMetadata? {
        val token = System.getenv(FIGMA_TOKEN_ENVIRONMENT_VARIABLE)?.takeIf(String::isNotBlank) ?: return null
        val nodeUrl = metadataNodeUrl.orNull ?: return null
        val namespace = metadataNamespace.orNull ?: return null
        val reference = FigmaNodeUrl.parse(nodeUrl)
        val node = runCatching {
            FigmaFileContentClient().getNodeContent(
                fileKey = reference.fileKey,
                token = token,
                nodeId = reference.nodeId,
                pluginData = "shared"
            )
        }.onFailure { failure ->
            logger.warn("Figma metadata is unavailable; selecting a full visual sync: ${failure.message}")
        }.getOrNull() ?: return null
        return FigmaSyncMetadataJson.read(node.sharedPluginData, namespace)
    }

    private fun buildWriter(tools: File, projectConfig: File) {
        run(
            tools,
            "node",
            "bin/build.mjs",
            "--project-config-json=${projectConfig.absolutePath}",
            "--output-dir=."
        )
    }

    private fun run(directory: File, vararg command: String) {
        val process = ProcessBuilder(platformCommand(command.toList()))
            .directory(directory)
            .inheritIO()
            .start()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw GradleException("Command '${command.joinToString(" ")}' failed with exit code $exitCode.")
        }
    }

    private fun capture(directory: File, vararg command: String): String {
        val process = ProcessBuilder(platformCommand(command.toList()))
            .directory(directory)
            .start()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        process.inputStream.use { input -> input.copyTo(output) }
        process.errorStream.use { input -> input.copyTo(error) }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw GradleException(
                "Command '${command.joinToString(" ")}' failed with exit code $exitCode: ${error.toString().trim()}"
            )
        }
        return output.toString().trim()
    }

    private fun platformCommand(command: List<String>): List<String> =
        if (isWindows() && command.first().endsWith(".cmd", ignoreCase = true)) {
            listOf("cmd.exe", "/d", "/c") + command
        } else {
            command
        }

    private fun npmExecutable(): String = if (isWindows()) "npm.cmd" else "npm"

    private fun isWindows(): Boolean = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)

    private companion object {
        const val FIGMA_TOKEN_ENVIRONMENT_VARIABLE = "FIGMA_FILE_CONTENT_ACCESS_TOKEN"
    }
}
