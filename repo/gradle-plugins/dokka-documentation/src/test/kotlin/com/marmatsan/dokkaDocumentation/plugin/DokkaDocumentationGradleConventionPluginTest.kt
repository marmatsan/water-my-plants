package com.marmatsan.dokkaDocumentation.plugin

import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

internal class DokkaDocumentationGradleConventionPluginTest :
    FunSpec(
        {
            test("configures strict public and internal API documentation") {
                given {
                    ProjectBuilder.builder().build()
                }.whenever { project ->
                    project.pluginManager.apply(DokkaDocumentationGradleConventionPlugin::class.java)
                    project.extensions.getByType(DokkaExtension::class.java)
                }.then { project, extension ->
                    extension.dokkaPublications
                        .named("html")
                        .get()
                        .failOnWarning
                        .get() shouldBe true
                    val sourceSet = extension.dokkaSourceSets.maybeCreate("main")
                    sourceSet.documentedVisibilities.get() shouldBe
                        setOf(
                            VisibilityModifier.Public,
                            VisibilityModifier.Internal
                        )
                    sourceSet.reportUndocumented.get() shouldBe true
                    val check = project.tasks.named("check").get()
                    check.taskDependencies
                        .getDependencies(check)
                        .map { task -> task.name }
                        .shouldContain("dokkaGenerate")
                }
            }
        }
    )
