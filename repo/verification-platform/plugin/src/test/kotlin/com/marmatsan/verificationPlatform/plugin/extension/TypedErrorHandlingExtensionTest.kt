package com.marmatsan.verificationPlatform.plugin.extension

import com.marmatsan.unitTest.dsl.given
import com.marmatsan.verificationPlatform.plugin.task.errorhandling.CheckTypedResultUsageTask
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainExactly
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

internal class TypedErrorHandlingExtensionTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "typed-error-handling-extension"
                )

            test("selects only production Kotlin below a repository scope") {
                val projectDirectory = temporaryDirectory.resolve("repository-scope")

                given {
                    val project =
                        ProjectBuilder
                            .builder()
                            .withProjectDir(projectDirectory.apply(File::mkdirs))
                            .build()
                    projectDirectory
                        .resolve("repo/tool/src/main/kotlin/example/Operation.kt")
                        .writeSource("package example")
                    projectDirectory
                        .resolve("repo/tool/src/test/kotlin/example/OperationTest.kt")
                        .writeSource("package example")
                    projectDirectory
                        .resolve("repo/tool/build/generated/source/Generated.kt")
                        .writeSource("package generated")
                    val task =
                        project.tasks.register(
                            "checkTypedResultUsage",
                            CheckTypedResultUsageTask::class.java
                        )

                    TypedErrorHandlingExtension(
                        project = project,
                        checkTypedResultUsage = task
                    ).productionSourceScope("repo")

                    task.get()
                }.whenever { task ->
                    task.inspectedFiles.files
                        .map { file ->
                            file.relativeTo(projectDirectory).path.replace(
                                oldChar = '\\',
                                newChar = '/'
                            )
                        }.sorted()
                }.then { relativePaths ->
                    relativePaths shouldContainExactly
                        listOf("repo/tool/src/main/kotlin/example/Operation.kt")
                }
            }

            test("rejects a blank production source scope") {
                given {
                    val project =
                        ProjectBuilder
                            .builder()
                            .withProjectDir(
                                temporaryDirectory
                                    .resolve("blank-scope")
                                    .apply(File::mkdirs)
                            ).build()
                    val task =
                        project.tasks.register(
                            "checkTypedResultUsage",
                            CheckTypedResultUsageTask::class.java
                        )

                    TypedErrorHandlingExtension(
                        project = project,
                        checkTypedResultUsage = task
                    )
                }.whenever { extension ->
                    shouldThrow<IllegalArgumentException> {
                        extension.productionSourceScope(" ")
                    }
                }.then { Unit }
            }
        }
    )

private fun File.writeSource(
    source: String
) {
    parentFile.mkdirs()
    writeText(source)
}
