package com.marmatsan.ci.plugin

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
@DisableCachingByDefault(because = "The Maven plugin writes and validates provider-generated configuration")
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
        logger.lifecycle("TeamCity Kotlin DSL validation passed.")
    }

    private fun isWindows(): Boolean =
        System.getProperty("os.name").lowercase(Locale.ROOT).contains("windows")
}
