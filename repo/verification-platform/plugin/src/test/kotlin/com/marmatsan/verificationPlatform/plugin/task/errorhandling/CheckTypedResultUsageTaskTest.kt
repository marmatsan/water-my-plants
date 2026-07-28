package com.marmatsan.verificationPlatform.plugin.task.errorhandling

import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

internal class CheckTypedResultUsageTaskTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "typed-result-usage-task",
                )

            test("remains inactive until a consumer configures a standard Result") {
                given {
                    typedResultFixture(
                        projectDirectory = temporaryDirectory.resolve("unconfigured"),
                        source = "import kotlin.Result",
                        acceptedResultQualifiedName = null,
                    )
                }.whenever { task ->
                    shouldNotThrowAny(task::checkTypedResultUsage)
                }.then { Unit }
            }

            test("accepts the configured product Result") {
                given {
                    typedResultFixture(
                        projectDirectory = temporaryDirectory.resolve("accepted"),
                        source =
                            """
                            package example

                            import com.github.michaelbull.result.Result

                            fun load(): Result<String, LoadError> = TODO()
                            """.trimIndent(),
                    )
                }.whenever { task ->
                    shouldNotThrowAny(task::checkTypedResultUsage)
                }.then { Unit }
            }

            test("rejects an incompatible product Result") {
                given {
                    typedResultFixture(
                        projectDirectory = temporaryDirectory.resolve("rejected"),
                        source =
                            """
                            package example

                            import kotlin.Result
                            """.trimIndent(),
                    )
                }.whenever { task ->
                    shouldThrow<IllegalStateException>(task::checkTypedResultUsage)
                }.then { Unit }
            }
        },
    )

private fun typedResultFixture(
    projectDirectory: File,
    source: String,
    acceptedResultQualifiedName: String? = "com.github.michaelbull.result.Result",
): CheckTypedResultUsageTask {
    val project =
        temporaryProject(
            projectDirectory = projectDirectory,
        )
    val sourceFile =
        project.projectDir
            .resolve("app/src/main/kotlin/example/Loader.kt")
            .apply {
                parentFile.mkdirs()
                writeText(source)
            }
    return project.tasks
        .register(
            "checkTypedResultUsage",
            CheckTypedResultUsageTask::class.java,
        ).get()
        .apply {
            repositoryRoot.set(project.layout.projectDirectory)
            acceptedResultQualifiedName?.let(this.acceptedResultQualifiedName::set)
            inspectedFiles.from(sourceFile)
        }
}

private fun temporaryProject(
    projectDirectory: File,
): Project =
    ProjectBuilder
        .builder()
        .withProjectDir(projectDirectory.apply(File::mkdirs))
        .build()
