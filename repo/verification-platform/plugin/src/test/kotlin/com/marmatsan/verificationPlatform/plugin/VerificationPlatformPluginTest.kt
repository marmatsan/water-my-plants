package com.marmatsan.verificationPlatform.plugin

import com.marmatsan.unitTest.dsl.given
import com.marmatsan.verificationPlatform.plugin.extension.VerificationPlatformExtension
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testfixtures.ProjectBuilder

internal class VerificationPlatformPluginTest :
    FunSpec(
        {
            test("registers every reviewed verification entry point on the root project") {
                given {
                    ProjectBuilder.builder().build()
                }.whenever { project ->
                    VerificationPlatformPlugin().apply(project)
                    project
                }.then { project ->
                    project.extensions.findByType(VerificationPlatformExtension::class.java).shouldNotBeNull()
                    val verificationTaskNames =
                        setOf(
                            "generateCiPlan",
                            "checkIncludedBuildVersions",
                            "checkModuleBoundaries",
                            "checkTypedResultUsage",
                            "checkGitWorkflow",
                            "checkDocumentation",
                            "checkRepositoryDiff",
                            "checkTeamCityDsl",
                            "prepareTeamCityCiPlan",
                            "generateCiTopologyPreview",
                            "runTeamCityInfrastructureHealth"
                        )
                    project.tasks.names shouldContainAll verificationTaskNames
                    verificationTaskNames.forEach { taskName ->
                        project.tasks
                            .named(taskName)
                            .get()
                            .group shouldBe "verification"
                    }
                }
            }

            test("rejects application outside the root project") {
                given {
                    val root = ProjectBuilder.builder().withName("root").build()
                    ProjectBuilder
                        .builder()
                        .withName("child")
                        .withParent(root)
                        .build()
                }.whenever { childProject ->
                    shouldThrow<IllegalArgumentException> {
                        VerificationPlatformPlugin().apply(childProject)
                    }
                }.then { failure ->
                    failure.message shouldContain "must be applied to the root project"
                }
            }
        }
    )
