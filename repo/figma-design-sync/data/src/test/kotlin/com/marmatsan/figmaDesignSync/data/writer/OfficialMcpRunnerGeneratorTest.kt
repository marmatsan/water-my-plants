package com.marmatsan.figmaDesignSync.data.writer

import com.marmatsan.figmaDesignSync.data.json.writer.ExecutableRunnerManifestJson
import com.marmatsan.figmaDesignSync.domain.model.writer.FigmaWriterRuntimeConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files

internal class OfficialMcpRunnerGeneratorTest : FunSpec({
    test("generates official visual and metadata runners entirely from Kotlin") {
        val root = Files.createTempDirectory("kotlin-mcp-runner")
        val tools = root.resolve("repo/figma-design-sync/tools")
        val sourceRoot = tools.resolve("src/app")
        Files.createDirectories(sourceRoot)
        Files.writeString(sourceRoot.resolve("writer.ts"), "export const writer = true;")
        val model = root.resolve("design-model.json")
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
                }
              }
            }
            """.trimIndent()
        )
        val script = tools.resolve("sync-trunk-design-model.mcp.js")
        Files.createDirectories(script.parent)
        Files.writeString(
            script,
            "const DESIGN_MODEL = undefined;\nconst SYNC_OPTIONS = undefined;\nreturn SYNC_OPTIONS;\n"
        )
        val policy = root.resolve("change-impact-policy.json")
        Files.writeString(
            policy,
            """
            {
              "schemaVersion":1,
              "documentationOnlyPaths":[],
              "figmaTransportOnlyPaths":[],
              "figmaModelNeutralPaths":[],
              "figmaModelContentPaths":[],
              "figmaVisualWriterPaths":[],
              "figmaVisualTargetRules":[]
            }
            """.trimIndent()
        )
        val output = root.resolve("out")
        val generator = OfficialMcpRunnerGenerator()
        val request = OfficialMcpRunnerGenerator.Request(
            modelPath = model.toString(),
            scriptPath = script.toString(),
            outputDirectory = output.toString(),
            toolsDirectory = tools.toString(),
            writerSourceDirectory = tools.resolve("src").toString(),
            repositoryRootDirectory = root.toString(),
            changeImpactPolicyPath = policy.toString(),
            config = runtimeConfig
        )
        val result = generator.generate(request)

        result.visualManifest.targets shouldContainExactly
            listOf("preflight", "versions", "waterMyPlants.libraries")
        result.visualManifest.fullVisualSync shouldBe true
        result.visualManifest.executionScopes.values shouldContainExactly listOf(
            "preflight",
            "versions",
            "waterMyPlants.libraries.androidx",
            "waterMyPlants.libraries.com",
            "waterMyPlants.libraries.cleanup"
        )
        result.visualManifest.payloadImage.shouldNotBeNull()
        result.metadataManifest.targets shouldContainExactly listOf("metadata")
        result.metadataManifest.writeMetadata shouldBe true

        val visualDirectory = output.resolve("visual")
        val stageSource = Files.readString(visualDirectory.resolve("10-stage-payload-from-png.mcp.js"))
        val androidxSource = Files.readString(
            visualDirectory.resolve("99-02-00-waterMyPlants-libraries-androidx.mcp.js")
        )
        stageSource shouldContain "for (const documentPage of figma.root.children)"
        stageSource shouldContain "page.setSharedPluginData(namespace, \"script\", payload.script)"
        androidxSource shouldContain
            "\"catalogRootFilters\":{\"waterMyPlants.libraries\":[\"androidx\"]}"
        ExecutableRunnerManifestJson().read(visualDirectory.resolve("manifest.json").toString())
            .manifestHash shouldBe result.visualManifest.manifestHash

        val chunkResult = generator.generate(
            request.copy(
                outputDirectory = root.resolve("chunks").toString(),
                transport = "chunks",
                chunkSize = 1_000
            )
        )
        chunkResult.visualManifest.payloadImage shouldBe null
        chunkResult.visualManifest.files.any { file -> file.startsWith("10-designModelJson-") } shouldBe true

        root.toFile().deleteRecursively()
    }
})

private val runtimeConfig = FigmaWriterRuntimeConfig(
    metadataPageId = "1:2",
    metadataNamespace = "test_sync",
    figmaFileKey = "file-key",
    projectDisplayName = "Test Project",
    mcpClientName = "test-client",
    repositoryRootRelativeToTools = "../../..",
    changeImpactPolicyRelativeToRepository = "change-impact-policy.json",
    writerTargetNames = listOf("preflight", "versions", "waterMyPlants.libraries", "metadata"),
    catalogTargetNames = listOf("waterMyPlants.libraries")
)
