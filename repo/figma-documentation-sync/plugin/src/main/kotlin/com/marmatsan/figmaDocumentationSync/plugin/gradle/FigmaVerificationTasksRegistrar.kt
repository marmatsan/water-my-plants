package com.marmatsan.figmaDocumentationSync.plugin.gradle

import com.marmatsan.figmaDocumentationSync.plugin.task.artifact.ValidateCanonicalFigmaArtifactSetTask
import com.marmatsan.figmaDocumentationSync.plugin.task.catalog.CheckFigmaCatalogUsageTask
import com.marmatsan.figmaDocumentationSync.plugin.task.ci.CheckCiExternalTopologyFreshnessTask
import com.marmatsan.figmaDocumentationSync.plugin.task.ci.CheckCiWindowsRuntimeFreshnessTask
import com.marmatsan.figmaDocumentationSync.plugin.task.impact.ClassifyFigmaChangeImpactTask
import com.marmatsan.figmaDocumentationSync.plugin.task.sync.CheckFigmaTrunkSyncTask
import com.marmatsan.figmaDocumentationSync.plugin.task.versions.CheckFigmaVersionNamingTask
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.io.File

/** Registers independent verification tasks and attaches stable checks to the Gradle lifecycle. */
internal class FigmaVerificationTasksRegistrar(
    private val context: FigmaPluginContext,
) {
    /** Registers independent verification tasks and their stable lifecycle dependencies. */
    fun register() {
        registerChangeImpactClassifier()
        registerArtifactValidation()
        registerLifecycleChecks()
        registerTrunkSyncCheck()
    }

    private fun registerChangeImpactClassifier() {
        context.project.tasks.register<ClassifyFigmaChangeImpactTask>("classifyFigmaChangeImpact") {
            group = "verification"
            description = "Classifies the current repository change for Figma verification and sync."
            configureChangeImpactInputs(context)
        }
    }

    private fun registerArtifactValidation() {
        val project = context.project
        project.tasks.register<ValidateCanonicalFigmaArtifactSetTask>("validateCanonicalFigmaArtifactSet") {
            group = "verification"
            description = "Validates a canonical main Figma artifact set and writes its handoff identity."
            artifactDirectory.set(
                project.layout.dir(
                    project.providers.gradleProperty("figmaArtifactDirectory").map(::File),
                ),
            )
            expectedGitSha.convention(project.providers.gradleProperty("figmaExpectedGitSha"))
            outputFile.set(
                project.layout
                    .file(
                        project.providers.gradleProperty("figmaArtifactValidationOutput").map(::File),
                    ).orElse(project.layout.buildDirectory.file("reports/figma-sync/validated-artifact-set.json")),
            )
        }
    }

    private fun registerLifecycleChecks() {
        val project = context.project
        val extension = context.extension
        val catalogUsage =
            project.tasks.register<CheckFigmaCatalogUsageTask>("checkFigmaCatalogUsage") {
                group = "verification"
                description = "Checks that dependency catalog entries rendered in Figma are used by the repository."
                configureCatalogInputs(context)
            }
        val versionNaming =
            project.tasks.register<CheckFigmaVersionNamingTask>("checkFigmaVersionNaming") {
                group = "verification"
                description = "Checks that dependency version keys follow the Figma section naming contract."
                versionsFile.set(extension.versionsFile)
            }
        val externalTopology =
            project.tasks.register<CheckCiExternalTopologyFreshnessTask>("checkCiExternalTopologyFreshness") {
                group = "verification"
                description = "Warns when the external CI topology has not been validated recently."
                ciExternalTopologyFile.set(extension.ciExternalTopologyFile)
                onlyIf("CI documentation adapter is enabled") {
                    extension.ciDocumentationEnabled.get()
                }
            }
        val windowsRuntime =
            project.tasks.register<CheckCiWindowsRuntimeFreshnessTask>("checkCiWindowsRuntimeFreshness") {
                group = "verification"
                description = "Warns when the Windows CI runtime has not been validated recently."
                ciWindowsRuntimeFile.set(extension.ciWindowsRuntimeFile)
                onlyIf("CI documentation adapter is enabled") {
                    extension.ciDocumentationEnabled.get()
                }
            }

        project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
            dependsOn(
                catalogUsage,
                versionNaming,
                externalTopology,
                windowsRuntime,
            )
        }
    }

    private fun registerTrunkSyncCheck() {
        context.project.tasks.register<CheckFigmaTrunkSyncTask>("checkFigmaTrunkSync") {
            group = "verification"
            description =
                "Checks that Figma sync metadata matches the design model generated from the current checkout."
            configureTrunkSyncInputs(context)
        }
    }
}
