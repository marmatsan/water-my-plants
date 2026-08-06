package com.marmatsan.figmaDocumentationSync.plugin.gradle

import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.gradle.api.tasks.Exec
import org.gradle.testfixtures.ProjectBuilder

internal class FigmaWriterTasksRegistrarTest :
    FunSpec(
        {
            test("writer build and test install their locked Node.js dependencies") {
                given {
                    ProjectBuilder
                        .builder()
                        .withProjectDir(
                            tempdir(
                                prefix = "figma-writer-tasks"
                            )
                        ).build()
                        .also { project ->
                            project.pluginManager.apply(FigmaDocumentationSyncGradlePlugin::class.java)
                            project.extensions
                                .getByType(figmaDocumentationSyncExtension::class.java)
                                .toolsDirectory
                                .set(project.layout.projectDirectory.dir("tools"))
                        }
                }.whenever { project ->
                    val installTask = project.tasks.getByName("installFigmaDocumentationSyncTools") as Exec
                    val buildTask = project.tasks.getByName("buildFigmaDocumentationSyncTools")
                    val testTask = project.tasks.getByName("testFigmaDocumentationSyncTools")

                    Triple(
                        installTask,
                        buildTask.taskDependencies.getDependencies(buildTask),
                        testTask.taskDependencies.getDependencies(testTask)
                    )
                }.then { (installTask, buildDependencies, testDependencies) ->
                    installTask.commandLine.last() shouldBe "ci"
                    buildDependencies shouldContain installTask
                    testDependencies shouldContain installTask
                }
            }
        }
    )
