package com.marmatsan.dokkaDocumentation.plugin

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
                // GIVEN
                val project = ProjectBuilder.builder().build()

                // WHEN
                project.pluginManager.apply(DokkaDocumentationGradleConventionPlugin::class.java)
                val extension = project.extensions.getByType(DokkaExtension::class.java)
                val publication = extension.dokkaPublications.named("html").get()
                val sourceSet = extension.dokkaSourceSets.maybeCreate("main")

                // THEN
                publication.failOnWarning.get() shouldBe true
                sourceSet.documentedVisibilities.get() shouldBe
                    setOf(
                        VisibilityModifier.Public,
                        VisibilityModifier.Internal,
                    )
                sourceSet.reportUndocumented.get() shouldBe true
                val check = project.tasks.named("check").get()
                check.taskDependencies
                    .getDependencies(check)
                    .map { task -> task.name }
                    .shouldContain("dokkaGenerate")
            }
        },
    )
