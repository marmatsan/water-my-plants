package com.marmatsan.figmaDesignSync.teamcityAdapter.configuration

import com.marmatsan.figmaDesignSync.domain.model.ci.CiTrigger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

internal class TeamCityGeneratedConfigurationReaderTest : FunSpec({

    test("read translates TeamCity pipelines jobs triggers artifacts checks and VCS roots") {
        // GIVEN
        val root = Files.createTempDirectory("teamcity-generated").toFile()
        val pipeline = root.resolve("Root_Ci").apply { mkdirs() }
        pipeline.resolve("project-config.xml").writeText(
            """
            <project>
              <name>CI</name>
            </project>
            """.trimIndent()
        )
        pipeline.resolve("pipeline.yml").writeText(
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
                features:
                  - type: commit-status-publisher
                    build_custom_name: TeamCity CI
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
            """.trimIndent()
        )
        pipeline.resolve("buildTypes").mkdirs()
        pipeline.resolve("buildTypes/Root_Ci.xml").writeText(
            """
            <build-type>
              <settings>
                <build-triggers>
                  <build-trigger id="TRIGGER_1" type="vcsTrigger">
                    <parameters>
                      <param name="branchFilter" value="+:*" />
                    </parameters>
                  </build-trigger>
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
            """.trimIndent()
        )
        writeVcsRoot(root)

        // WHEN
        val configuration = TeamCityGeneratedConfigurationReader().read(root)

        // THEN
        val actualPipeline = configuration.pipelines.single()
        actualPipeline.id shouldBe "Root_Ci"
        actualPipeline.name shouldBe "CI"
        actualPipeline.triggers[0] shouldBe CiTrigger(
            type = CiTrigger.Type.Vcs,
            branchFilter = "+:*",
            dependencyPipelineId = null,
            afterSuccessfulBuildOnly = null
        )
        actualPipeline.triggers[1] shouldBe CiTrigger(
            type = CiTrigger.Type.Schedule,
            branchFilter = "+:<default>",
            dependencyPipelineId = null,
            afterSuccessfulBuildOnly = null
        )

        val job = actualPipeline.jobs.single()
        job.id shouldBe "verify"
        job.name shouldBe "Verify"
        job.steps.single().command shouldBe ".\\gradlew.bat check"
        job.repositoryIds shouldBe listOf("Root_GitHub")
        job.artifacts.single().path shouldBe "build/report.json"
        job.dependencies.single().artifactPaths shouldBe listOf("build/input.json")
        job.publishedChecks.single().name shouldBe "TeamCity CI"

        val vcsRoot = configuration.vcsRoots.single()
        vcsRoot.id shouldBe "Root_GitHub"
        vcsRoot.name shouldBe "water-my-plants"
        vcsRoot.defaultBranchRef shouldBe "refs/heads/main"
        vcsRoot.branchSpec shouldBe listOf("#! fallbackToDefault: false", "+:refs/heads/(*)")
    }
})

private fun writeVcsRoot(root: File) {
    val vcsRoots = root.resolve("Root/vcsRoots").apply { mkdirs() }
    vcsRoots.resolve("Root_GitHub.xml").writeText(
        """
        <vcs-root type="jetbrains.git">
          <name>water-my-plants</name>
          <param name="branch" value="refs/heads/main" />
          <param name="branchSpec"><![CDATA[#! fallbackToDefault: false
        +:refs/heads/(*)]]></param>
          <param name="url" value="https://github.com/marmatsan/water-my-plants.git" />
        </vcs-root>
        """.trimIndent()
    )
}
