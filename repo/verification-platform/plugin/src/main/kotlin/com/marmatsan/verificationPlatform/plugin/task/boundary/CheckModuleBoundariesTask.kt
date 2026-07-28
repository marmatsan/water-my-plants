package com.marmatsan.verificationPlatform.plugin.task.boundary

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Enforces repository-configured source and included-build dependency boundaries. */
@DisableCachingByDefault(
    because = "This validation produces no reusable output artifact",
)
abstract class CheckModuleBoundariesTask : DefaultTask() {
    /** Repository root containing the inspected scopes. */
    @get:Internal
    abstract val repositoryRoot: DirectoryProperty

    /** Reusable source scopes relative to [repositoryRoot]. */
    @get:Input
    abstract val reusableScopePaths: ListProperty<String>

    /** Forbidden references for each reusable source scope, joined with an internal separator. */
    @get:Input
    abstract val forbiddenReferencesByScope: MapProperty<String, String>

    /** Kotlin sources and Gradle scripts inspected by this task. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inspectedFiles: ConfigurableFileCollection

    /** Fails when a reusable scope directly knows a forbidden implementation or sibling build. */
    @TaskAction
    fun checkModuleBoundaries() {
        val root = repositoryRoot.get().asFile
        val files = inspectedFiles.files.filter { file -> file.isFile }
        val failures =
            reusableScopePaths.get().flatMap { scopePath ->
                val scope = root.resolve(scopePath).canonicalFile
                val forbiddenReferences =
                    forbiddenReferencesByScope
                        .get()[scopePath]
                        .orEmpty()
                        .split(REFERENCE_SEPARATOR)
                        .filter(String::isNotEmpty)
                files
                    .filter { file -> file.canonicalFile.toPath().startsWith(scope.toPath()) }
                    .flatMap { file ->
                        val normalizedContent =
                            file.readText().replace(
                                '\\',
                                '/',
                            )
                        buildList {
                            if (
                                file.name == "settings.gradle.kts" &&
                                RELATIVE_INCLUDED_BUILD.containsMatchIn(normalizedContent)
                            ) {
                                add(
                                    "${relativePath(
                                        root = root,
                                        file = file,
                                    )} must not include a sibling build by relative path",
                                )
                            }
                            forbiddenReferences
                                .filter(normalizedContent::contains)
                                .forEach { reference ->
                                    add(
                                        "${relativePath(
                                            root = root,
                                            file = file,
                                        )} must not reference $reference",
                                    )
                                }
                        }
                    }
            }

        check(failures.isEmpty()) {
            failures.distinct().sorted().joinToString(
                prefix = "Module boundary verification failed:\n- ",
                separator = "\n- ",
            )
        }
    }

    private fun relativePath(
        root: java.io.File,
        file: java.io.File,
    ): String =
        file.relativeTo(root).path.replace(
            oldChar = '\\',
            newChar = '/',
        )

    internal companion object {
        /** Non-printing delimiter used to serialize forbidden references through a Gradle map property. */
        const val REFERENCE_SEPARATOR = "\u001F"

        /** Detects forbidden sibling included-build wiring in a settings script. */
        val RELATIVE_INCLUDED_BUILD = Regex("""includeBuild\s*\(\s*[\"']\.\./""")
    }
}
