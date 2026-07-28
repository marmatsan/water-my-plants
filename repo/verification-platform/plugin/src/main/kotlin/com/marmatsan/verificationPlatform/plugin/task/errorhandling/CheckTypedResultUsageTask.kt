package com.marmatsan.verificationPlatform.plugin.task.errorhandling

import com.github.michaelbull.result.fold
import com.marmatsan.verificationPlatform.domain.service.errorhandling.TypedResultUsageValidator
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Enforces the configured typed-result implementation across product Kotlin sources. */
@DisableCachingByDefault(
    because = "This validation produces no reusable output artifact",
)
abstract class CheckTypedResultUsageTask : DefaultTask() {
    /** Repository root used to report stable relative source paths. */
    @get:Internal
    abstract val repositoryRoot: DirectoryProperty

    /** Optional fully qualified `Result` type accepted by the product architecture. */
    @get:Input
    @get:Optional
    abstract val acceptedResultQualifiedName: Property<String>

    /** Product Kotlin sources inspected by this task. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inspectedFiles: ConfigurableFileCollection

    /** Fails when product code imports or declares an incompatible `Result` contract. */
    @TaskAction
    fun checkTypedResultUsage() {
        val acceptedResult = acceptedResultQualifiedName.orNull ?: return
        val root = repositoryRoot.get().asFile
        val validator =
            TypedResultUsageValidator(
                acceptedResultQualifiedName = acceptedResult,
            )
        val violations =
            inspectedFiles.files
                .filter { file -> file.isFile }
                .flatMap { file ->
                    validator
                        .validate(
                            relativePath =
                                file.relativeTo(root).path.replace(
                                    oldChar = '\\',
                                    newChar = '/',
                                ),
                            source = file.readText(),
                        ).fold(
                            { emptyList() },
                            { error -> error.violations },
                        )
                }.sortedWith(
                    compareBy(
                        { violation -> violation.relativePath },
                        { violation -> violation.lineNumber },
                        { violation -> violation.reason },
                    ),
                )

        check(violations.isEmpty()) {
            violations.joinToString(
                prefix = "Typed Result verification failed:\n- ",
                separator = "\n- ",
            ) { violation ->
                "${violation.relativePath}:${violation.lineNumber} ${violation.reason}"
            }
        }
    }
}
