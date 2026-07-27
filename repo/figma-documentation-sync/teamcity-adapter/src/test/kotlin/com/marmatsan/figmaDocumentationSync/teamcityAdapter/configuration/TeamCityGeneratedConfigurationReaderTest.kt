package com.marmatsan.figmaDocumentationSync.teamcityAdapter.configuration

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiJob
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiTrigger
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import java.io.File

internal class TeamCityGeneratedConfigurationReaderTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "teamcity-generated-configuration",
                )

            test("read translates TeamCity pipelines jobs triggers artifacts and VCS roots") {
                given {
                    val root = temporaryDirectory.resolve("configuration").apply { mkdirs() }
                    val pipeline =
                        root
                            .resolve(
                                relative = "Root_Ci",
                            ).apply { mkdirs() }
                    pipeline
                        .resolve(
                            relative = "project-config.xml",
                        ).writeText(
                            """
                            <project>
                              <name>CI</name>
                            </project>
                            """.trimIndent(),
                        )
                    pipeline
                        .resolve(
                            relative = "pipeline.yml",
                        ).writeText(
                            """
                            version: 1
                            jobs:
                              verify:
                                name: Verify
                                steps:
                                  - script-content: .\gradlew.bat check
                                    name: Run Gradle check
                                    id: RUNNER_1
                                    type: script
                                repositories:
                                  - Root_GitHub:
                                      enabled: true
                                      path: ""
                                files-publication:
                                  - path: build/report.json
                                    publish-artifact: true
                                    share-with-jobs: true
                                dependencies:
                                  - prepare:
                                      files:
                                        - build/input.json
                            """.trimIndent(),
                        )
                    pipeline
                        .resolve(
                            relative = "buildTypes",
                        ).mkdirs()
                    pipeline
                        .resolve(
                            relative = "buildTypes/Root_Ci.xml",
                        ).writeText(
                            """
                            <build-type>
                              <settings>
                                <build-triggers>
                                  <build-trigger id="TRIGGER_2" type="schedulingTrigger">
                                    <parameters>
                                      <param name="branchFilter" value="+:&lt;default&gt;" />
                                      <param name="hour" value="6" />
                                      <param name="minute" value="0" />
                                      <param name="schedulingPolicy" value="daily" />
                                    </parameters>
                                  </build-trigger>
                                </build-triggers>
                              </settings>
                            </build-type>
                            """.trimIndent(),
                        )
                    writeStatusGate(
                        root = root,
                    )
                    writeVcsRoot(
                        root = root,
                    )
                    root
                }.whenever { root ->
                    TeamCityGeneratedConfigurationReader().read(root)
                }.then { configuration ->
                    val actualPipeline = configuration.pipelines.single()
                    actualPipeline.id shouldBe "Root_Ci"
                    actualPipeline.name shouldBe "CI"
                    actualPipeline.triggers[0] shouldBe
                        CiTrigger(
                            type = CiTrigger.Type.Vcs,
                            branchFilter = "+:*",
                            dependencyPipelineId = null,
                            afterSuccessfulBuildOnly = null,
                        )
                    actualPipeline.triggers[1] shouldBe
                        CiTrigger(
                            type = CiTrigger.Type.Schedule,
                            branchFilter = "+:<default>",
                            dependencyPipelineId = null,
                            afterSuccessfulBuildOnly = null,
                        )

                    val job = actualPipeline.jobs.single()
                    job.id shouldBe "verify"
                    job.name shouldBe "Verify"
                    job.steps.single().command shouldBe ".\\gradlew.bat check"
                    job.repositoryIds shouldBe listOf("Root_GitHub")
                    job.artifacts.single().path shouldBe "build/report.json"
                    job.dependencies.single().artifactPaths shouldBe listOf("build/input.json")
                    job.publishedChecks shouldBe
                        listOf(
                            CiJob.PublishedCheck(
                                name = "TeamCity CI",
                            ),
                        )

                    val vcsRoot = configuration.vcsRoots.single()
                    vcsRoot.id shouldBe "Root_GitHub"
                    vcsRoot.name shouldBe "water-my-plants"
                    vcsRoot.defaultBranchRef shouldBe "refs/heads/main"
                    vcsRoot.branchSpec shouldBe
                        listOf(
                            "#! fallbackToDefault: false",
                            "+:refs/heads/(*)",
                        )
                }
            }
        },
    )

private fun writeStatusGate(
    root: File,
) {
    val buildTypes =
        root
            .resolve(
                relative = "Root/buildTypes",
            ).apply { mkdirs() }
    buildTypes
        .resolve(
            relative = "Root_CiGate.xml",
        ).writeText(
            """
            <build-type>
              <settings>
                <options>
                  <option name="buildConfigurationType" value="COMPOSITE" />
                </options>
                <build-triggers>
                  <build-trigger id="TRIGGER_1" type="vcsTrigger">
                    <parameters>
                      <param name="branchFilter" value="+:*" />
                    </parameters>
                  </build-trigger>
                </build-triggers>
                <build-extensions>
                  <extension id="BUILD_EXT_1" type="commit-status-publisher">
                    <parameters>
                      <param name="build_custom_name" value="TeamCity CI" />
                    </parameters>
                  </extension>
                </build-extensions>
                <dependencies>
                  <depend-on sourceBuildTypeId="Root_Ci" />
                </dependencies>
              </settings>
            </build-type>
            """.trimIndent(),
        )
}

private fun writeVcsRoot(
    root: File,
) {
    val vcsRoots =
        root
            .resolve(
                relative = "Root/vcsRoots",
            ).apply { mkdirs() }
    vcsRoots
        .resolve(
            relative = "Root_GitHub.xml",
        ).writeText(
            """
            <vcs-root type="jetbrains.git">
              <name>water-my-plants</name>
              <param name="branch" value="refs/heads/main" />
              <param name="branchSpec"><![CDATA[#! fallbackToDefault: false
            +:refs/heads/(*)]]></param>
              <param name="url" value="https://github.com/marmatsan/water-my-plants.git" />
            </vcs-root>
            """.trimIndent(),
        )
}
