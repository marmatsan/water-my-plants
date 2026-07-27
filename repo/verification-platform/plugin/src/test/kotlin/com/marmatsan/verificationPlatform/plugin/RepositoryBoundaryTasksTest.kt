package com.marmatsan.verificationPlatform.plugin

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class RepositoryBoundaryTasksTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "repository-boundary-tasks",
                )

            test("accepts local version ownership and independent settings") {
                val project =
                    temporaryProject(
                        projectDirectory = temporaryDirectory.resolve("local-versions"),
                    )
                val build = project.projectDir.resolve("tooling").apply(File::mkdirs)
                build.resolve("versions.properties").writeText("kotlinVersion=2.4.0")
                build.resolve("settings.gradle.kts").writeText(
                    "val versions = file(\"versions.properties\")",
                )
                val task =
                    project.tasks
                        .register(
                            "checkVersions",
                            CheckIncludedBuildVersionsTask::class.java,
                        ).get()
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.includedBuildPaths.set(listOf("tooling"))
                task.includedBuildConfigurationFiles.from(build.listFiles())

                shouldNotThrowAny(task::checkIncludedBuildVersions)
            }

            test("rejects a version registry owned by a sibling build") {
                val project =
                    temporaryProject(
                        projectDirectory = temporaryDirectory.resolve("sibling-versions"),
                    )
                val build = project.projectDir.resolve("tooling").apply(File::mkdirs)
                build.resolve("versions.properties").writeText("kotlinVersion=2.4.0")
                build.resolve("settings.gradle.kts").writeText(
                    "val versions = file(\"../other/versions.properties\")",
                )
                val task =
                    project.tasks
                        .register(
                            "checkVersions",
                            CheckIncludedBuildVersionsTask::class.java,
                        ).get()
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.includedBuildPaths.set(listOf("tooling"))
                task.includedBuildConfigurationFiles.from(build.listFiles())

                shouldThrow<IllegalStateException>(task::checkIncludedBuildVersions)
            }

            test("rejects relative sibling includes and configured implementation references") {
                val project =
                    temporaryProject(
                        projectDirectory = temporaryDirectory.resolve("forbidden-references"),
                    )
                val scope = project.projectDir.resolve("tooling").apply(File::mkdirs)
                val settings = scope.resolve("settings.gradle.kts")
                settings.writeText("includeBuild(\"../implementation\")")
                val source =
                    scope.resolve("src/main/kotlin/Adapter.kt").apply {
                        parentFile.mkdirs()
                        writeText("import com.example.product.ConcreteAdapter")
                    }
                val task =
                    project.tasks
                        .register(
                            "checkBoundaries",
                            CheckModuleBoundariesTask::class.java,
                        ).get()
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.reusableScopePaths.set(listOf("tooling"))
                task.forbiddenReferencesByScope.set(
                    mapOf("tooling" to "com.example.product"),
                )
                task.inspectedFiles.from(
                    settings,
                    source,
                )

                shouldThrow<IllegalStateException>(task::checkModuleBoundaries)
            }
        },
    )

private fun temporaryProject(
    projectDirectory: File,
): Project =
    ProjectBuilder
        .builder()
        .withProjectDir(projectDirectory.apply(File::mkdirs))
        .build()
