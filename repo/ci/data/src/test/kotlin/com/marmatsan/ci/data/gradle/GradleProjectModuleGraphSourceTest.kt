package com.marmatsan.ci.data.gradle

import com.marmatsan.ci.domain.model.ModuleDependency
import com.marmatsan.ci.domain.model.RepositoryModule
import com.marmatsan.ci.domain.model.RepositoryModuleGraph
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import org.gradle.testfixtures.ProjectBuilder

class GradleProjectModuleGraphSourceTest : FunSpec({
    test("reads real module directories and project dependencies from Gradle") {
        val rootDirectory = Files.createTempDirectory("ci-module-graph").toFile()

        try {
            val root = ProjectBuilder.builder()
                .withName("root")
                .withProjectDir(rootDirectory)
                .build()
            val coreParent = childProject(root, "core")
            val coreUi = childProject(coreParent, "ui", withBuildFile = true)
            val app = childProject(root, "app", withBuildFile = true)
            val onboardingParent = childProject(root, "onboarding")
            val onboardingUi = childProject(onboardingParent, "ui", withBuildFile = true)
            app.configurations.create("implementation")
            onboardingUi.configurations.create("implementation")
            app.dependencies.add("implementation", app.dependencies.project(mapOf("path" to coreUi.path)))
            onboardingUi.dependencies.add(
                "implementation",
                onboardingUi.dependencies.project(mapOf("path" to coreUi.path))
            )

            GradleProjectModuleGraphSource().read(root) shouldBe RepositoryModuleGraph(
                modules = listOf(
                    RepositoryModule(id = ":app", directory = "app"),
                    RepositoryModule(id = ":core:ui", directory = "core/ui"),
                    RepositoryModule(id = ":onboarding:ui", directory = "onboarding/ui")
                ),
                dependencies = listOf(
                    ModuleDependency(dependentModule = ":app", dependencyModule = ":core:ui"),
                    ModuleDependency(dependentModule = ":onboarding:ui", dependencyModule = ":core:ui")
                )
            )
        } finally {
            rootDirectory.deleteRecursively()
        }
    }
}) {
    companion object {
        private fun childProject(
            parent: org.gradle.api.Project,
            name: String,
            withBuildFile: Boolean = false
        ): org.gradle.api.Project {
            val directory = File(parent.projectDir, name).apply(File::mkdirs)
            if (withBuildFile) File(directory, "build.gradle.kts").writeText("")
            return ProjectBuilder.builder()
                .withName(name)
                .withParent(parent)
                .withProjectDir(directory)
                .build()
        }
    }
}
