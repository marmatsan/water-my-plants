package com.marmatsan.verificationPlatform.plugin.task.documentation

import com.marmatsan.verificationPlatform.data.documentation.FileSystemDocumentationSource
import com.marmatsan.verificationPlatform.data.json.CiPlanJson
import com.marmatsan.verificationPlatform.data.json.DocumentationCoverageJson
import com.marmatsan.verificationPlatform.domain.service.documentation.DocumentationValidator
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Validates repository documentation and committed change coverage in Kotlin. */
@DisableCachingByDefault(
    because = "Documentation coverage depends on the committed Git change set"
)
abstract class CheckDocumentationTask : DefaultTask() {
    /** Repository checkout containing Markdown and canonical source files. */
    @get:Internal
    abstract val repositoryRoot: DirectoryProperty

    /** Versioned implementation-to-documentation coverage manifest. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val coverageManifest: RegularFileProperty

    /** Provider-neutral CI plan containing the committed paths to validate. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val planFile: RegularFileProperty

    /** Runs structural, link, source, review, and coverage validation. */
    @TaskAction
    fun checkDocumentation() {
        val root = repositoryRoot.get().asFile
        val plan = CiPlanJson().read(planFile.get().asFile.readText())
        val rules = DocumentationCoverageJson().read(coverageManifest.get().asFile.readText())
        val result =
            DocumentationValidator().validate(
                snapshot = FileSystemDocumentationSource().read(root),
                coverageRules = rules,
                changedPaths = plan.changedFiles
            )

        result.warnings.forEach(logger::warn)
        if (result.errors.isNotEmpty()) {
            throw GradleException("Documentation validation failed:\n${result.errors.joinToString("\n")}")
        }
        if (result.coverageViolations.isNotEmpty()) {
            val details =
                result.coverageViolations.joinToString("\n") { violation ->
                    "[${violation.rule}] changed: ${violation.changedSources.joinToString()}; " +
                        "update one of: ${violation.requiredDocumentation.joinToString()}"
                }
            throw GradleException("Documentation coverage is incomplete.\n$details")
        }

        logger.lifecycle(
            "Documentation validation passed for {} typed documents.",
            result.validatedDocuments.size
        )
    }
}
