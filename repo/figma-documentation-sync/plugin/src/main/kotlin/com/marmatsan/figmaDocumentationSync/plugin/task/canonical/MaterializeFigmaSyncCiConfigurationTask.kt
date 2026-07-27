package com.marmatsan.figmaDocumentationSync.plugin.task.canonical

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/** Runs a consumer-owned CI configuration command without retaining a Gradle project reference. */
@DisableCachingByDefault(
    because = "The external command owns incremental behavior for its generated configuration",
)
abstract class MaterializeFigmaSyncCiConfigurationTask
    @Inject
    constructor(
        private val execOperations: ExecOperations,
    ) : DefaultTask() {
        /** Whether the consumer enabled CI documentation in its Figma model. */
        @get:Input
        abstract val ciDocumentationEnabled: Property<Boolean>

        /** External command and arguments selected by the consumer adapter. */
        @get:Input
        abstract val ciConfigurationCommand: ListProperty<String>

        /** Classified change-impact artifact that decides whether materialization is required. */
        @get:InputFile
        @get:PathSensitive(PathSensitivity.RELATIVE)
        abstract val changeImpactFile: RegularFileProperty

        /** Working directory selected by the consumer adapter. */
        @get:Internal
        abstract val ciConfigurationWorkingDirectory: DirectoryProperty

        /** Generated CI configuration consumed by the design-model task. */
        @get:OutputDirectory
        abstract val ciGeneratedConfigurationDirectory: DirectoryProperty

        init {
            onlyIf("CI documentation adapter is enabled and Figma impact requires full verification") {
                ciDocumentationEnabled.get() &&
                    ciConfigurationCommand.get().isNotEmpty() &&
                    CanonicalFigmaFullVerificationResolver.fromChangeImpact(changeImpactFile.get().asFile)
            }
            outputs.upToDateWhen { false }
        }

        /** Executes the configured command through Gradle's configuration-cache-safe execution service. */
        @TaskAction
        fun materialize() {
            execOperations.exec {
                workingDir(ciConfigurationWorkingDirectory.get().asFile)
                commandLine(ciConfigurationCommand.get())
            }
        }
    }
