package com.marmatsan.verificationPlatform.plugin.task.boundary

import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.string.shouldContain
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
                given {
                    includedBuildVersionsFixture(
                        projectDirectory = temporaryDirectory.resolve("local-versions"),
                        versionsByBuild = mapOf("tooling" to "kotlinVersion=2.4.0"),
                    )
                }.whenever { task ->
                    shouldNotThrowAny(task::checkIncludedBuildVersions)
                }.then { Unit }
            }

            test("rejects a version registry owned by a sibling build") {
                given {
                    includedBuildVersionsFixture(
                        projectDirectory = temporaryDirectory.resolve("sibling-versions"),
                        versionsByBuild = mapOf("tooling" to "kotlinVersion=2.4.0"),
                        settingsByBuild =
                            mapOf(
                                "tooling" to "val versions = file(\"../other/versions.properties\")",
                            ),
                    )
                }.whenever { task ->
                    shouldThrow<IllegalStateException>(task::checkIncludedBuildVersions)
                }.then { Unit }
            }

            test("accepts an aligned version omitted by non-consuming builds") {
                given {
                    includedBuildVersionsFixture(
                        projectDirectory = temporaryDirectory.resolve("aligned-versions"),
                        versionsByBuild =
                            mapOf(
                                "first" to "kotlinResultLibraryVersion=2.3.1",
                                "second" to "kotlinResultLibraryVersion=2.3.1",
                                "non-consumer" to "kotlinVersion=2.4.0",
                            ),
                        alignedVersions = listOf("kotlinResultLibraryVersion"),
                    )
                }.whenever { task ->
                    shouldNotThrowAny(task::checkIncludedBuildVersions)
                }.then { Unit }
            }

            test("rejects version drift between consuming included builds") {
                given {
                    includedBuildVersionsFixture(
                        projectDirectory = temporaryDirectory.resolve("misaligned-versions"),
                        versionsByBuild =
                            mapOf(
                                "first" to "kotlinResultLibraryVersion=2.3.1",
                                "second" to "kotlinResultLibraryVersion=2.4.0",
                            ),
                        alignedVersions = listOf("kotlinResultLibraryVersion"),
                    )
                }.whenever { task ->
                    shouldThrow<IllegalStateException>(task::checkIncludedBuildVersions)
                }.then { failure ->
                    failure.message.orEmpty() shouldContain
                        "kotlinResultLibraryVersion must align across consuming included builds"
                }
            }

            test("rejects relative sibling includes and configured implementation references") {
                given {
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
                    project.tasks
                        .register(
                            "checkBoundaries",
                            CheckModuleBoundariesTask::class.java,
                        ).get()
                        .apply {
                            repositoryRoot.set(project.layout.projectDirectory)
                            reusableScopePaths.set(listOf("tooling"))
                            forbiddenReferencesByScope.set(
                                mapOf("tooling" to "com.example.product"),
                            )
                            inspectedFiles.from(
                                settings,
                                source,
                            )
                        }
                }.whenever { task ->
                    shouldThrow<IllegalStateException>(task::checkModuleBoundaries)
                }.then { Unit }
            }
        },
    )

private fun includedBuildVersionsFixture(
    projectDirectory: File,
    versionsByBuild: Map<String, String>,
    settingsByBuild: Map<String, String> = emptyMap(),
    alignedVersions: List<String> = emptyList(),
): CheckIncludedBuildVersionsTask {
    val project =
        temporaryProject(
            projectDirectory = projectDirectory,
        )
    val configurationFiles =
        versionsByBuild.flatMap { (relativePath, versions) ->
            val build = project.projectDir.resolve(relativePath).apply(File::mkdirs)
            val versionsFile = build.resolve("versions.properties").apply { writeText(versions) }
            val settingsFile =
                build.resolve("settings.gradle.kts").apply {
                    writeText(
                        settingsByBuild[relativePath]
                            ?: "val versions = file(\"versions.properties\")",
                    )
                }
            listOf(
                settingsFile,
                versionsFile,
            )
        }
    return project.tasks
        .register(
            "checkVersions",
            CheckIncludedBuildVersionsTask::class.java,
        ).get()
        .apply {
            repositoryRoot.set(project.layout.projectDirectory)
            includedBuildPaths.set(versionsByBuild.keys.toList())
            includedBuildConfigurationFiles.from(configurationFiles)
            alignedVersionProperties.set(alignedVersions)
        }
}

private fun temporaryProject(
    projectDirectory: File,
): Project =
    ProjectBuilder
        .builder()
        .withProjectDir(projectDirectory.apply(File::mkdirs))
        .build()
