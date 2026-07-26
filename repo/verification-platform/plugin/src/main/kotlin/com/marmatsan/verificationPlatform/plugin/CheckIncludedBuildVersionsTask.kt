package com.marmatsan.verificationPlatform.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Verifies that every repository included build owns its version registry. */
abstract class CheckIncludedBuildVersionsTask : DefaultTask() {
    /** Repository root containing the included builds. */
    @get:Internal
    abstract val repositoryRoot: DirectoryProperty

    /** Included-build directories relative to [repositoryRoot]. */
    @get:Input
    abstract val includedBuildPaths: ListProperty<String>

    /** Settings and version registries inspected by this task. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val includedBuildConfigurationFiles: ConfigurableFileCollection

    /** Fails when an included build has no local registry or reads another build's registry. */
    @TaskAction
    fun checkIncludedBuildVersions() {
        val repositoryDirectory = repositoryRoot.get().asFile
        val failures =
            includedBuildPaths.get().flatMap { relativePath ->
                val buildDirectory = repositoryDirectory.resolve(relativePath)
                val versionsFile = buildDirectory.resolve("versions.properties")
                val settingsFile = buildDirectory.resolve("settings.gradle.kts")
                buildList {
                    if (!versionsFile.isFile) {
                        add("$relativePath must own versions.properties")
                    }
                    if (!settingsFile.isFile) {
                        add("$relativePath must own settings.gradle.kts")
                    } else {
                        val settings =
                            settingsFile.readText().replace(
                                oldChar = '\\',
                                newChar = '/',
                            )
                        val externalVersionRegistry = EXTERNAL_VERSION_REGISTRY.find(settings)?.value
                        if (externalVersionRegistry != null) {
                            add(
                                "$relativePath/settings.gradle.kts must not read another build's registry: " +
                                    externalVersionRegistry,
                            )
                        }
                        if ("file(\"versions.properties\")" !in settings) {
                            add("$relativePath/settings.gradle.kts must read its local versions.properties")
                        }
                    }
                }
            }

        check(failures.isEmpty()) {
            failures.joinToString(
                prefix = "Included-build version ownership failed:\n- ",
                separator = "\n- ",
            )
        }
    }

    private companion object {
        val EXTERNAL_VERSION_REGISTRY = Regex("""\.\./[^\"']+/versions\.properties""")
    }
}
