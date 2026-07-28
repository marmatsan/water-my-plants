package com.marmatsan.verificationPlatform.plugin.task.boundary

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
import org.gradle.work.DisableCachingByDefault
import java.util.Properties

/** Verifies included-build version ownership and configured cross-build alignment. */
@DisableCachingByDefault(
    because = "This validation produces no reusable output artifact",
)
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

    /** Version property names that must agree whenever multiple included builds declare them. */
    @get:Input
    abstract val alignedVersionProperties: ListProperty<String>

    /** Fails when version ownership or a configured alignment contract is violated. */
    @TaskAction
    fun checkIncludedBuildVersions() {
        val repositoryDirectory = repositoryRoot.get().asFile
        val ownershipFailures =
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
        val alignmentFailures =
            alignedVersionProperties.getOrElse(emptyList()).flatMap { propertyName ->
                val declarations =
                    includedBuildPaths.get().mapNotNull { relativePath ->
                        val versionsFile =
                            repositoryDirectory
                                .resolve(relativePath)
                                .resolve("versions.properties")
                        if (!versionsFile.isFile) {
                            null
                        } else {
                            val properties =
                                Properties().apply {
                                    versionsFile.inputStream().use(::load)
                                }
                            properties.getProperty(propertyName)?.let { value ->
                                relativePath to value
                            }
                        }
                    }
                if (declarations.map { (_, value) -> value }.distinct().size > 1) {
                    listOf(
                        "$propertyName must align across consuming included builds: " +
                            declarations.joinToString { (relativePath, value) ->
                                "$relativePath=$value"
                            },
                    )
                } else {
                    emptyList()
                }
            }
        val failures = ownershipFailures + alignmentFailures

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
