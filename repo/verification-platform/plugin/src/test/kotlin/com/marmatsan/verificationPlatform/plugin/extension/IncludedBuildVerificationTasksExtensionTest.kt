package com.marmatsan.verificationPlatform.plugin.extension

import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.gradle.api.tasks.Exec
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

internal class IncludedBuildVerificationTasksExtensionTest :
    FunSpec(
        {
            test("binds an isolated Gradle build with explicit composition properties") {
                given {
                    val projectDirectory =
                        tempdir(
                            prefix = "isolated-gradle-build-task"
                        )
                    val project =
                        ProjectBuilder
                            .builder()
                            .withProjectDir(projectDirectory)
                            .build()
                    val buildDirectory =
                        projectDirectory
                            .resolve("repo/tooling")
                            .apply(File::mkdirs)

                    project to
                        IncludedBuildVerificationTasksExtension(project)
                            .isolatedGradleBuildTask(
                                name = "verifyToolingDistribution",
                                buildDirectory = buildDirectory,
                                taskPath = ":verifyStagedPublication",
                                projectProperties =
                                    mapOf(
                                        "dependencySourceBuild" to "C:/source/dependency"
                                    ),
                                description = "Verifies the tooling distribution."
                            ).get()
                }.whenever { (project, task) ->
                    IsolatedTaskFixture(
                        expectedWrapper =
                            project.rootProject.file(
                                if (
                                    System.getProperty("os.name").startsWith(
                                        "Windows",
                                        ignoreCase = true
                                    )
                                ) {
                                    "gradlew.bat"
                                } else {
                                    "gradlew"
                                }
                            ),
                        task = task
                    )
                }.then { fixture ->
                    fixture.task.workingDir shouldBe
                        fixture.task.project.projectDir
                            .resolve("repo/tooling")
                    fixture.task.commandLine shouldContainExactly
                        listOf(
                            fixture.expectedWrapper.absolutePath,
                            "--no-daemon",
                            ":verifyStagedPublication",
                            "-PdependencySourceBuild=C:/source/dependency",
                            "--stacktrace"
                        )
                }
            }
        }
    )

private data class IsolatedTaskFixture(
    val expectedWrapper: File,
    val task: Exec
)
