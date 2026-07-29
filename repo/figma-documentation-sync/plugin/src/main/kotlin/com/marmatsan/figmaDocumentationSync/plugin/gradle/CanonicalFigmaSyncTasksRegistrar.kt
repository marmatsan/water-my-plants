package com.marmatsan.figmaDocumentationSync.plugin.gradle

import com.marmatsan.figmaDocumentationSync.plugin.task.canonical.CheckCanonicalFigmaTrunkSyncTask
import com.marmatsan.figmaDocumentationSync.plugin.task.canonical.GenerateCanonicalFigmaSyncModelTask
import com.marmatsan.figmaDocumentationSync.plugin.task.canonical.MaterializeFigmaSyncCiConfigurationTask
import com.marmatsan.figmaDocumentationSync.plugin.task.canonical.PrepareCanonicalFigmaSyncTask
import com.marmatsan.figmaDocumentationSync.plugin.task.canonical.ValidateCanonicalFigmaSyncScopeTask
import com.marmatsan.figmaDocumentationSync.plugin.task.impact.ClassifyFigmaChangeImpactTask
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.register

/** Registers the ordered tasks that prepare and verify one canonical Figma synchronization scope. */
internal class CanonicalFigmaSyncTasksRegistrar(
    private val context: FigmaPluginContext
) {
    /** Registers the canonical classification, generation, validation, and verification task chain. */
    fun register() {
        val project = context.project
        val extension = context.extension
        val cleanReports =
            project.tasks.register<Delete>("cleanCanonicalFigmaSyncReports") {
                group = "build"
                description = "Removes stale canonical Figma Sync reports before preparing a new scope."
                delete(project.layout.buildDirectory.dir("reports/figma-sync"))
            }
        val phasedExecution = context.booleanProperty("figmaCanonicalTeamCityPhasedExecution")
        val classifyChange =
            project.tasks.register<ClassifyFigmaChangeImpactTask>("classifyCanonicalFigmaSyncChangeImpact") {
                group = "verification"
                description = "Classifies the main revision used by the canonical Figma Sync pipeline."
                dependsOn(cleanReports)
                configureChangeImpactInputs(context)
            }
        val materializeCi =
            project.tasks.register<MaterializeFigmaSyncCiConfigurationTask>(
                "materializeFigmaSyncCiConfiguration"
            ) {
                group = "documentation"
                description = "Runs the optional CI adapter when the Figma model can change."
                if (phasedExecution.get()) {
                    mustRunAfter(classifyChange)
                } else {
                    dependsOn(classifyChange)
                }
                ciDocumentationEnabled.set(extension.ciDocumentationEnabled)
                ciConfigurationCommand.set(extension.ciConfigurationCommand)
                changeImpactFile.set(extension.changeImpactFile)
                ciConfigurationWorkingDirectory.set(extension.ciConfigurationWorkingDirectory)
                ciGeneratedConfigurationDirectory.set(extension.ciGeneratedConfigurationDirectory)
            }
        val generateModel =
            project.tasks.register<GenerateCanonicalFigmaSyncModelTask>("generateCanonicalFigmaSyncModel") {
                group = "documentation"
                description = "Generates the model required by the prepared canonical Figma Sync scope."
                if (phasedExecution.get()) {
                    mustRunAfter(materializeCi)
                } else {
                    dependsOn(materializeCi)
                }
                changeImpactFile.set(extension.changeImpactFile)
                configureDesignModelInputs(context)
                outputFile.set(extension.designModelFile)
            }

        project.tasks.register<PrepareCanonicalFigmaSyncTask>("prepareCanonicalFigmaSync") {
            group = "documentation"
            description = "Prepares the canonical model, MCP runners, visual plan, and shared sync scope."
            if (phasedExecution.get()) {
                mustRunAfter(generateModel)
            } else {
                dependsOn(generateModel)
            }
            changeImpactFile.set(extension.changeImpactFile)
            changeImpactPolicyFile.set(extension.changeImpactPolicyFile)
            designModelFile.set(extension.designModelFile)
            metadataNodeUrl.set(extension.designModelMetadataNodeUrl)
            metadataNamespace.set(extension.metadataNamespace)
            projectRootDirectory.set(project.layout.projectDirectory)
            toolsDirectory.set(extension.toolsDirectory)
            runnerOutputDirectory.set(project.layout.buildDirectory.dir("reports/figma-sync/mcp-runners"))
            visualSyncPlanFile.set(project.layout.buildDirectory.file("reports/figma-sync/visual-sync-plan.json"))
            scopeFile.set(project.layout.buildDirectory.file("reports/figma-sync/sync-scope.json"))
            runnerTransport.convention(
                project.providers.gradleProperty("figmaMcpTransport").orElse("png")
            )
            runnerChunkSize.convention(
                project.providers
                    .gradleProperty("figmaMcpChunkSize")
                    .map(String::toInt)
                    .orElse(12_000)
            )
            outputs.upToDateWhen { false }
        }

        val verifiedScopeOutput = project.layout.buildDirectory.file("tmp/figma-sync/verified-scope.txt")
        val validateScope =
            project.tasks.register<ValidateCanonicalFigmaSyncScopeTask>("validateCanonicalFigmaSyncScope") {
                group = "verification"
                description = "Validates the canonical scope artifact before the conditional Figma metadata check."
                scopeFile.set(project.layout.buildDirectory.file("reports/figma-sync/sync-scope.json"))
                designModelFile.set(extension.designModelFile)
                projectRootDirectory.set(project.layout.projectDirectory)
                verifiedScopeFile.set(verifiedScopeOutput)
                outputs.upToDateWhen { false }
            }
        val checkTrunkSync =
            project.tasks.register<CheckCanonicalFigmaTrunkSyncTask>("checkCanonicalFigmaTrunkSync") {
                group = "verification"
                description = "Checks Figma metadata only when the validated canonical scope can change the model."
                if (phasedExecution.get()) {
                    mustRunAfter(validateScope)
                } else {
                    dependsOn(validateScope)
                }
                verifiedScopeFile.set(verifiedScopeOutput)
                configureTrunkSyncInputs(context)
            }
        project.tasks.register("verifyCanonicalFigmaSync") {
            group = "verification"
            description = "Validates the shared canonical scope and conditionally checks Figma trunk metadata."
            dependsOn(checkTrunkSync)
        }
    }
}
