package com.marmatsan.verificationPlatform.data.gradle

import com.marmatsan.verificationPlatform.domain.model.modules.ModuleDependency
import com.marmatsan.verificationPlatform.domain.model.modules.RepositoryModule
import com.marmatsan.verificationPlatform.domain.model.modules.RepositoryModuleGraph
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.nio.file.Files

class GradleProjectModuleGraphSourceTest :
    FunSpec(
        {
            test("reads real module directories and project dependencies from Gradle") {
                val rootDirectory = Files.createTempDirectory("ci-module-graph").toFile()

                try {
                    val root =
                        ProjectBuilder
                            .builder()
                            .withName("root")
                            .withProjectDir(rootDirectory)
                            .build()
                    val coreParent =
                        childProject(
                            parent = root,
                            name = "core"
                        )
                    val coreUi =
                        childProject(
                            parent = coreParent,
                            name = "ui",
                            withBuildFile = true
                        )
                    val app =
                        childProject(
                            parent = root,
                            name = "app",
                            withBuildFile = true
                        )
                    val onboardingParent =
                        childProject(
                            parent = root,
                            name = "onboarding"
                        )
                    val onboardingUi =
                        childProject(
                            parent = onboardingParent,
                            name = "ui",
                            withBuildFile = true
                        )
                    app.configurations.create("implementation")
                    onboardingUi.configurations.create("implementation")
                    app.dependencies.add(
                        "implementation",
                        app.dependencies.project(mapOf("path" to coreUi.path))
                    )
                    onboardingUi.dependencies.add(
                        "implementation",
                        onboardingUi.dependencies.project(mapOf("path" to coreUi.path))
                    )

                    GradleProjectModuleGraphSource().read(root) shouldBe
                        RepositoryModuleGraph(
                            modules =
                                listOf(
                                    RepositoryModule(
                                        id = ":app",
                                        directory = "app"
                                    ),
                                    RepositoryModule(
                                        id = ":core:ui",
                                        directory = "core/ui"
                                    ),
                                    RepositoryModule(
                                        id = ":onboarding:ui",
                                        directory = "onboarding/ui"
                                    )
                                ),
                            dependencies =
                                listOf(
                                    ModuleDependency(
                                        dependentModule = ":app",
                                        dependencyModule = ":core:ui"
                                    ),
                                    ModuleDependency(
                                        dependentModule = ":onboarding:ui",
                                        dependencyModule = ":core:ui"
                                    )
                                )
                        )
                } finally {
                    rootDirectory.deleteRecursively()
                }
            }
        }
    ) {
    companion object {
        private fun childProject(
            parent: org.gradle.api.Project,
            name: String,
            withBuildFile: Boolean = false
        ): org.gradle.api.Project {
            val directory =
                File(
                    parent.projectDir,
                    name
                ).apply(File::mkdirs)
            if (withBuildFile) {
                File(
                    directory,
                    "build.gradle.kts"
                ).writeText("")
            }
            return ProjectBuilder
                .builder()
                .withName(name)
                .withParent(parent)
                .withProjectDir(directory)
                .build()
        }
    }
}
