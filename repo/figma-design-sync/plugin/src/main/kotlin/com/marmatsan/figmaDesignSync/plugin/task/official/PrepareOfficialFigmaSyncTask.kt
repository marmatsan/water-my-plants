package com.marmatsan.figmaDesignSync.plugin.task.official

import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaVerificationScope
import com.marmatsan.figmaDesignSync.domain.model.sync.OfficialFigmaSyncScope
import com.marmatsan.figmaDesignSync.plugin.di.create
import com.marmatsan.figmaDesignSync.plugin.di.figmaDesignSyncComponent
import java.io.ByteArrayOutputStream
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Builds the official MCP runner artifacts and writes their shared sync scope. */
@DisableCachingByDefault(because = "Runs npm and Node against the official model artifact")
abstract class PrepareOfficialFigmaSyncTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val changeImpactFile: RegularFileProperty

    @get:Internal
    abstract val designModelFile: RegularFileProperty

    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    @get:Internal
    abstract val toolsDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val runnerOutputDirectory: DirectoryProperty

    @get:Internal
    abstract val visualSyncPlanFile: RegularFileProperty

    @get:OutputFile
    abstract val scopeFile: RegularFileProperty

    /** Runs the writer toolchain only for a full verification, then writes `sync-scope.json`. */
    @TaskAction
    fun prepare() {
        val component = figmaDesignSyncComponent::class.create()
        val scopeJson = component.officialFigmaSyncScopeJson
        val impact = scopeJson.readChangeImpact(changeImpactFile.get().asFile.absolutePath)
        val gitSha = capture(projectRootDirectory.get().asFile, "git", "rev-parse", "HEAD")

        val scope = if (impact.scope == FigmaVerificationScope.FULL_VERIFICATION) {
            val model = designModelFile.get().asFile
            if (!model.isFile) {
                throw GradleException("Missing official Figma design model artifact: ${model.path}")
            }

            val tools = toolsDirectory.get().asFile
            run(tools, npmExecutable(), "ci")
            run(tools, npmExecutable(), "run", "build")

            val runnerDirectory = runnerOutputDirectory.get().asFile
            run(
                tools,
                "node",
                "dist/write-mcp-runner.mjs",
                "--mode=official",
                "--model=${model.absolutePath}",
                "--out-dir=${runnerDirectory.absolutePath}"
            )
            run(
                tools,
                "node",
                "dist/write-mcp-runner.mjs",
                "--mode=official",
                "--model=${model.absolutePath}",
                "--target=metadata",
                "--out-dir=${runnerDirectory.absolutePath}"
            )

            val manifests = scopeJson.readRunnerManifests(runnerDirectory.absolutePath)
            val visualManifest = manifests.singleOrNull { manifest -> manifest.fullVisualSync }
                ?: throw GradleException("Official visual MCP runner manifest was not generated exactly once.")
            val metadataManifest = manifests.singleOrNull { manifest -> manifest.writeMetadata }
                ?: throw GradleException("Official metadata MCP runner manifest was not generated exactly once.")
            val plan = visualSyncPlanFile.get().asFile
            run(
                projectRootDirectory.get().asFile,
                "node",
                tools.resolve("dist/write-visual-sync-plan.mjs").absolutePath,
                "--manifest=${visualManifest.path}",
                "--out=${plan.absolutePath}"
            )
            val visualPlan = scopeJson.readVisualSyncPlan(plan.absolutePath)

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
                visualSyncDecision = visualPlan.decision,
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
}
