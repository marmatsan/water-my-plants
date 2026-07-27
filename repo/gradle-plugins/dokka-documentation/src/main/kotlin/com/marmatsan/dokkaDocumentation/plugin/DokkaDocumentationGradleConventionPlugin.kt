package com.marmatsan.dokkaDocumentation.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

/**
 * Applies the repository's strict Dokka contract to a Kotlin module.
 *
 * The convention owns documentation visibility, warning policy, source links,
 * and verification lifecycle wiring so consuming modules do not reproduce
 * those decisions independently.
 */
@Suppress("unused")
class DokkaDocumentationGradleConventionPlugin : Plugin<Project> {
    override fun apply(
        project: Project,
    ) {
        project.pluginManager.apply("org.jetbrains.dokka")

        val remoteSourceRootUrl =
            project.providers
                .gradleProperty(REMOTE_SOURCE_ROOT_URL_PROPERTY)
                .map { url -> url.trimEnd('/') }

        project.extensions.configure<DokkaExtension> {
            moduleName.convention(
                project.path.removePrefix(":").replace(
                    ':',
                    '/',
                ),
            )

            dokkaPublications.configureEach {
                failOnWarning.set(true)
            }

            dokkaSourceSets.configureEach {
                documentedVisibilities.set(
                    setOf(
                        VisibilityModifier.Public,
                        VisibilityModifier.Internal,
                    ),
                )
                reportUndocumented.set(true)
                skipEmptyPackages.set(true)
                suppressGeneratedFiles.set(true)

                val localSourceDirectory = project.layout.projectDirectory.dir("src/main/kotlin")
                if (localSourceDirectory.asFile.exists()) {
                    sourceLink {
                        localDirectory.set(localSourceDirectory)
                        remoteUrl(
                            remoteSourceRootUrl.map { sourceRootUrl ->
                                "$sourceRootUrl/" +
                                    localSourceDirectory.asFile
                                        .relativeTo(project.rootProject.projectDir)
                                        .invariantSeparatorsPath
                            },
                        )
                        remoteLineSuffix.set("#L")
                    }
                }
            }
        }

        project.tasks.named("check").configure {
            dependsOn("dokkaGenerate")
        }
    }

    private companion object {
        const val REMOTE_SOURCE_ROOT_URL_PROPERTY = "dokkaDocumentation.remoteSourceRootUrl"
    }
}
