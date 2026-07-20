package com.marmatsan.figmaDocumentationSync.data.writer

import com.marmatsan.figmaDocumentationSync.data.json.writer.ExecutableRunnerManifestJson
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaWriterRuntimeConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files

internal class OfficialMcpRunnerGeneratorTest : FunSpec(
    {
    test("generates official visual and metadata runners entirely from Kotlin") {
        val root = Files.createTempDirectory("kotlin-mcp-runner")
        val tools = root.resolve(
            "repo/figma-documentation-sync/tools"
        )
        val sourceRoot = tools.resolve(
            "src/app"
        )
        Files.createDirectories(sourceRoot)
        Files.writeString(
            sourceRoot.resolve(
                "writer.ts"
            ),
            "export const writer = true;"
        )
        val model = root.resolve(
            "design-model.json"
        )
        Files.writeString(
            model,
            """
            {
              "branch":"main",
              "gitSha":"abc123",
              "modelHash":"sha256:model",
              "content":{
                "versions":{"kotlin":"2.4.0"},
                "catalogs":{
                  "waterMyPlants":{
                    "libraries":[{"group":"androidx"},{"group":"com"}]
                  }
                },
                "ci":{
                  "externalTopology":{
                    "schemaVersion":1,
                    "validation":{"lastValidatedOn":"2026-07-18","warnAfterDays":90},
                    "nodes":[{"id":"operator","type":"actor","name":"Operator","description":"Starts actions."}],
                    "connections":[]
                  },
                  "windowsRuntime":{
                    "schemaVersion":1,
                    "validation":{"lastValidatedOn":"2026-07-18","warnAfterDays":90},
                    "platform":"Windows",
                    "services":[{
                      "id":"teamcity-server",
                      "name":"TeamCity Server",
                      "description":"Hosts TeamCity.",
                      "service":"TeamCity",
                      "startup":"Automatic",
                      "identity":"LocalSystem"
                    }]
                  },
                  "teamCity":{
                    "vcsRoots":[],
                    "pipelines":[
                      {
                        "id":"Root_Ci",
                        "name":"CI",
                        "triggers":[],
                        "jobs":[{
                          "id":"verify",
                          "name":"Verify",
                          "steps":[],
                          "repositoryIds":[],
                          "artifacts":[],
                          "dependencies":[],
                          "publishedChecks":[{"name":"TeamCity CI"}]
                        }]
                      },
                      {"id":"Root_FigmaSync","name":"Figma Sync","triggers":[],"jobs":[]}
                    ]
                  }
                }
              }
            }
            """.trimIndent()
        )
        val script = tools.resolve(
            "sync-trunk-design-model.mcp.js"
        )
        Files.createDirectories(script.parent)
        Files.writeString(
            script,
            "const DESIGN_MODEL = undefined;\nconst SYNC_OPTIONS = undefined;\nreturn SYNC_OPTIONS;\n"
        )
        val policy = root.resolve(
            "change-impact-policy.json"
        )
        Files.writeString(
            policy,
            """
            {
              "schemaVersion":1,
              "documentationOnlyPaths":[],
              "figmaTransportOnlyPaths":[],
              "figmaModelNeutralPaths":[],
              "figmaModelContentPaths":[],
              "figmaVisualWriterPaths":["repo/figma-documentation-sync/tools/src/*"],
              "figmaVisualTargetRules":[]
            }
            """.trimIndent()
        )
        val output = root.resolve(
            "out"
        )
        val generator = OfficialMcpRunnerGenerator()
        val request = OfficialMcpRunnerGenerator.Request(
            modelPath = model.toString(),
            scriptPath = script.toString(),
            outputDirectory = output.toString(),
            toolsDirectory = tools.toString(),
            writerSourceDirectory = tools.resolve(
                "src"
            ).toString(),
            repositoryRootDirectory = root.toString(),
            changeImpactPolicyPath = policy.toString(),
            config = runtimeConfig
        )
        val result = generator.generate(
            request = request
        )

        result.visualManifest.targets shouldContainExactly
            listOf(
                "preflight",
                "versions",
                "waterMyPlants.libraries",
                "ci.windowsRuntime"
            )
        result.visualManifest.fullVisualSync shouldBe true
        result.visualManifest.executionScopes.values shouldContainExactly listOf(
            "preflight",
            "versions",
            "waterMyPlants.libraries.androidx",
            "waterMyPlants.libraries.com",
            "waterMyPlants.libraries.cleanup",
            "ci.windowsRuntime"
        )
        result.visualManifest.payloadImage.shouldNotBeNull()
        result.metadataManifest.targets shouldContainExactly listOf("metadata")
        result.metadataManifest.writeMetadata shouldBe true

        val visualDirectory = output.resolve(
            "visual"
        )
        val stageSource = Files.readString(
            visualDirectory.resolve(
                "10-stage-payload-from-png.mcp.js"
            )
        )
        val androidxSource = Files.readString(
            visualDirectory.resolve(
                "99-02-00-waterMyPlants-libraries-androidx.mcp.js"
            )
        )
        val ciSource = Files.readString(
            visualDirectory.resolve(
                "99-03-ci-windowsRuntime.mcp.js"
            )
        )
        stageSource shouldContain "for (const documentPage of figma.root.children)"
        stageSource shouldContain "page.setSharedPluginData(namespace, \"script\", payload.script)"
        androidxSource shouldContain
            "\"catalogRootFilters\":{\"waterMyPlants.libraries\":[\"androidx\"]}"
        ciSource shouldContain "\"ciVisualPlan\""
        ciSource shouldContain "\"target\":\"ci.windowsRuntime\""
        ExecutableRunnerManifestJson().read(
            visualDirectory.resolve(
                "manifest.json"
            ).toString()
        )
            .manifestHash shouldBe result.visualManifest.manifestHash

        val chunkResult = generator.generate(
            request = request.copy(
                outputDirectory = root.resolve(
                    "chunks"
                ).toString(),
                transport = "chunks",
                chunkSize = 1_000
            )
        )
        chunkResult.visualManifest.payloadImage shouldBe null
        chunkResult.visualManifest.files.any { file -> file.startsWith(
            prefix = "10-designModelJson-"
        ) } shouldBe true

        root.toFile().deleteRecursively()
    }
}
)

private val runtimeConfig = FigmaWriterRuntimeConfig(
    metadataPageId = "1:2",
    metadataNamespace = "test_sync",
    figmaFileKey = "file-key",
    projectDisplayName = "Test Project",
    mcpClientName = "test-client",
    repositoryRootRelativeToTools = "../../..",
    changeImpactPolicyRelativeToRepository = "change-impact-policy.json",
    writerTargetNames = listOf(
        "preflight",
        "versions",
        "waterMyPlants.libraries",
        "ci.windowsRuntime",
        "metadata"
    ),
    catalogTargetNames = listOf("waterMyPlants.libraries"),
    ciVisualPlanConfig = CiVisualPlanConfig(
        configurationModelName = "teamCity",
        ciPipelineName = "CI",
        figmaPipelineName = "Figma Sync",
        githubMainBlobUrl = "https://example.test/blob/main",
        teamCitySource = ".teamcity/settings.kts",
        topologySource = "docs/ci/external-topology.yaml",
        windowsRuntimeSource = "docs/ci/windows-runtime.yaml",
        windowsRuntimeRunbookSource = "docs/runbooks/teamcity.md",
        visualContractSource = "docs/ci/visual-model-contract.md",
        branchProtectionSource = "docs/ci/main-branch-protection.md",
        officialSyncSource = "docs/runbooks/official-sync.md",
        officialDesignModelPath = "build/reports/figma-sync/design-model.json"
    )
)
