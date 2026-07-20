package com.marmatsan.verificationPlatform.plugin

import java.util.Locale
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault

/** Exposes TeamCity Kotlin DSL generation through the Gradle verification API. */
@DisableCachingByDefault(
    because = "The Maven plugin writes and validates provider-generated configuration"
)
abstract class CheckTeamCityDslTask : DefaultTask() {
    /** Repository checkout containing the Maven wrapper and TeamCity project. */
    @get:Internal
    abstract val repositoryRoot: DirectoryProperty

    /** Maven project that generates the effective TeamCity configuration. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val teamCityPom: RegularFileProperty

    /** Injected process boundary used only by this provider-specific adapter. */
    @get:Inject
    protected abstract val execOperations: ExecOperations

    /** Generates and validates the TeamCity Kotlin DSL with the Maven wrapper. */
    @TaskAction
    fun checkTeamCityDsl() {
        val root = repositoryRoot.get().asFile
        val wrapper = root.resolve(if (isWindows()) "mvnw.cmd" else "mvnw")
        check(wrapper.isFile) { "Maven wrapper was not found: $wrapper" }

        execOperations.exec { spec ->
            spec.workingDir(root)
            spec.commandLine(
                wrapper.absolutePath,
                "-f",
                teamCityPom.get().asFile.absolutePath,
                "teamcity-configs:generate"
            )
        }.assertNormalExitValue()
        validateGeneratedTeamCityConfiguration(
            directory = root.resolve(GENERATED_CONFIG_DIRECTORY)
        )
        logger.lifecycle("TeamCity Kotlin DSL validation passed.")
    }

    private fun validateGeneratedTeamCityConfiguration(
        directory: java.io.File
    ) {
        val pipelineFiles = directory
            .walkTopDown()
            .filter { file -> file.isFile && file.name == PIPELINE_FILE_NAME }
            .toList()
        check(pipelineFiles.isNotEmpty()) {
            "TeamCity generation did not produce any $PIPELINE_FILE_NAME files under $directory"
        }

        pipelineFiles.forEach { file ->
            check(UNSUPPORTED_STATUS_PUBLISHER !in file.readText()) {
                "${file.path} contains '$UNSUPPORTED_STATUS_PUBLISHER'. " +
                    "Commit Status Publisher must be configured on the classic CI gate because it " +
                    "is not a supported Pipeline YAML job feature."
            }
        }

        val buildTypeFiles = directory
            .walkTopDown()
            .filter { file -> file.isFile && file.extension == XML_EXTENSION }
            .toList()
        val ciGate = buildTypeFiles.singleOrNull { file ->
            file.name.endsWith("_${CI_GATE_ID}.$XML_EXTENSION")
        }
        check(ciGate != null) {
            "TeamCity generation did not produce the versioned $CI_GATE_ID build configuration."
        }

        val ciGateXml = ciGate.readText()
        REQUIRED_CI_GATE_FRAGMENTS.forEach { fragment ->
            check(fragment in ciGateXml) {
                "${ciGate.path} is missing the required CI gate contract '$fragment'."
            }
        }
        check(CI_PIPELINE_DEPENDENCY_REGEX.containsMatchIn(ciGateXml)) {
            "${ciGate.path} does not snapshot-depend on $CI_PIPELINE_ID."
        }

        val ciPipeline = buildTypeFiles.singleOrNull { file ->
            file.name.endsWith("_${CI_PIPELINE_ID}.$XML_EXTENSION")
        }
        check(ciPipeline != null) {
            "TeamCity generation did not produce the $CI_PIPELINE_ID pipeline head."
        }
        check(VCS_TRIGGER_FRAGMENT !in ciPipeline.readText()) {
            "${ciPipeline.path} still owns a VCS trigger; $CI_GATE_ID must be the single automatic entry point."
        }
    }

    private fun isWindows(): Boolean =
        System.getProperty("os.name").lowercase(Locale.ROOT).contains("windows")

    private companion object {
        const val GENERATED_CONFIG_DIRECTORY = ".teamcity/target/generated-configs"
        const val PIPELINE_FILE_NAME = "pipeline.yml"
        const val XML_EXTENSION = "xml"
        const val CI_PIPELINE_ID = "WaterMyPlantsCi"
        const val CI_GATE_ID = "WaterMyPlantsCiGate"
        const val UNSUPPORTED_STATUS_PUBLISHER = "type: commit-status-publisher"
        const val VCS_TRIGGER_FRAGMENT = "type=\"vcsTrigger\""
        val CI_PIPELINE_DEPENDENCY_REGEX = Regex("sourceBuildTypeId=\"[^\"]*${CI_PIPELINE_ID}\"")
        val REQUIRED_CI_GATE_FRAGMENTS = listOf(
            "name=\"buildConfigurationType\" value=\"COMPOSITE\"",
            VCS_TRIGGER_FRAGMENT,
            "type=\"commit-status-publisher\"",
            "name=\"build_custom_name\" value=\"TeamCity CI\""
        )
    }
}
