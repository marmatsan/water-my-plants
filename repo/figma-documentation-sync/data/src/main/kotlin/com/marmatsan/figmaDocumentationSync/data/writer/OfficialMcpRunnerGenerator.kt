package com.marmatsan.figmaDocumentationSync.data.writer

import com.marmatsan.figmaDocumentationSync.data.datasource.impact.FigmaChangeImpactPolicyDataSource
import com.marmatsan.figmaDocumentationSync.data.fingerprint.FigmaTargetFingerprintCalculator
import com.marmatsan.figmaDocumentationSync.data.fingerprint.WriterScopeFingerprintCalculator
import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.CanonicalJson
import com.marmatsan.figmaDocumentationSync.data.json.visual.CiVisualPlanJson
import com.marmatsan.figmaDocumentationSync.data.json.writer.ExecutableRunnerManifestJson
import com.marmatsan.figmaDocumentationSync.data.png.PayloadPngEncoder
import com.marmatsan.figmaDocumentationSync.domain.model.writer.ExecutableRunnerManifest
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaWriterRuntimeConfig
import com.marmatsan.figmaDocumentationSync.domain.model.writer.OfficialSyncPayload
import com.marmatsan.figmaDocumentationSync.domain.model.writer.RunnerPayloadImage
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Generates official visual and metadata MCP runner directories without Node orchestration. */
class OfficialMcpRunnerGenerator(
    private val renderer: McpRunnerSourceRenderer = McpRunnerSourceRenderer(),
    private val targetFingerprints: FigmaTargetFingerprintCalculator = FigmaTargetFingerprintCalculator(),
    private val writerFingerprints: WriterScopeFingerprintCalculator = WriterScopeFingerprintCalculator(),
    private val manifestJson: ExecutableRunnerManifestJson = ExecutableRunnerManifestJson(),
    private val payloadEncoder: PayloadPngEncoder = PayloadPngEncoder(),
    private val policySource: FigmaChangeImpactPolicyDataSource = FigmaChangeImpactPolicyDataSource()
) {
    fun generate(request: Request): Result {
        require(request.transport in SUPPORTED_TRANSPORTS) {
            "Unsupported MCP transport '${request.transport}'. Expected png or chunks."
        }
        require(request.chunkSize >= MINIMUM_CHUNK_SIZE) {
            "MCP chunk size must be at least $MINIMUM_CHUNK_SIZE."
        }
        val modelPath = Path.of(request.modelPath).toAbsolutePath().normalize()
        val scriptPath = Path.of(request.scriptPath).toAbsolutePath().normalize()
        val outputRoot = Path.of(request.outputDirectory).toAbsolutePath().normalize()
        val modelElement = Json.parseToJsonElement(Files.readString(modelPath).removePrefix(UTF8_BOM))
        val designModel = modelElement.jsonObject
        val modelJson = Json.encodeToString(JsonElement.serializer(), modelElement)
        val script = Files.readString(scriptPath)
        validateModel(designModel)
        validateStagingEntry("designModelJson", modelJson)
        validateStagingEntry("script", script)

        val modelHash = designModel.requiredString("modelHash")
        val gitSha = designModel.requiredString("gitSha")
        val writerHash = Sha256Hash.of(script.toByteArray(StandardCharsets.UTF_8))
        val transportHash = transportHash(request)
        val allTargetFingerprints = targetFingerprints.create(
            designModel = designModel,
            visualTargets = request.config.visualTargetNames,
            catalogTargets = request.config.catalogTargetNames
        )
        val allWriterFingerprints = writerFingerprints.create(
            sourceRoot = Path.of(request.writerSourceDirectory).toAbsolutePath().normalize(),
            repositoryRoot = Path.of(request.repositoryRootDirectory).toAbsolutePath().normalize(),
            policy = policySource.read(request.changeImpactPolicyPath),
            writerTargets = request.config.writerTargetNames,
            catalogTargets = request.config.catalogTargetNames,
            scopes = allTargetFingerprints.keys.toList()
        )
        val context = Context(
            request = request,
            designModel = designModel,
            modelJson = modelJson,
            script = script,
            modelHash = modelHash,
            gitSha = gitSha,
            writerHash = writerHash,
            transportHash = transportHash,
            targetFingerprints = allTargetFingerprints,
            writerScopeFingerprints = allWriterFingerprints
        )

        val visual = generateRunner(
            context = context,
            outputDirectory = outputRoot.resolve(VISUAL_DIRECTORY),
            targets = request.config.visualTargetNames,
            writeMetadata = false,
            fullVisualSync = true
        )
        val metadata = generateRunner(
            context = context,
            outputDirectory = outputRoot.resolve(METADATA_DIRECTORY),
            targets = listOf("metadata"),
            writeMetadata = true,
            fullVisualSync = false
        )
        return Result(visualManifest = visual, metadataManifest = metadata)
    }

    private fun generateRunner(
        context: Context,
        outputDirectory: Path,
        targets: List<String>,
        writeMetadata: Boolean,
        fullVisualSync: Boolean
    ): ExecutableRunnerManifest {
        recreate(outputDirectory)
        val sources = linkedMapOf<String, String>()
        val namespace = context.request.config.officialStagingNamespace
        sources[CLEAR_STAGING_FILE] = renderer.clearStaging(context.request.config.metadataPageId, namespace)

        val payloadImage = if (context.request.transport == TRANSPORT_PNG) {
            val payload = OfficialSyncPayload(
                payloadSchemaVersion = PayloadPngEncoder.PAYLOAD_SCHEMA_VERSION,
                designModelJson = context.modelJson,
                designModelHash = context.modelHash,
                designModelGitSha = context.gitSha,
                designModelLength = context.modelJson.length,
                script = context.script,
                scriptLength = context.script.length,
                writerHash = context.writerHash,
                transportHash = context.transportHash
            )
            val bytes = payloadEncoder.encode(payloadEncoder.payloadJson(payload))
            require(bytes.size <= PayloadPngEncoder.MAX_FIGMA_UPLOAD_ASSET_BYTES) {
                "Official payload PNG is ${bytes.size} bytes and exceeds " +
                    "${PayloadPngEncoder.MAX_FIGMA_UPLOAD_ASSET_BYTES} bytes. Use chunk transport."
            }
            Files.write(outputDirectory.resolve(PAYLOAD_PNG_FILE), bytes)
            val image = RunnerPayloadImage(
                fileName = PAYLOAD_PNG_FILE,
                byteLength = bytes.size,
                sha256 = Sha256Hash.of(bytes),
                textKeyword = PayloadPngEncoder.TEXT_KEYWORD
            )
            sources[STAGE_PAYLOAD_FILE] = renderer.stagePayloadFromPng(
                metadataPageId = context.request.config.metadataPageId,
                namespace = namespace,
                payloadFileName = PAYLOAD_PNG_FILE,
                identity = expectedIdentity(context)
            )
            image
        } else {
            addChunkSources(sources, context, namespace)
            null
        }

        sources[FINALIZE_STAGING_FILE] = renderer.finalizeStaging(
            metadataPageId = context.request.config.metadataPageId,
            namespace = namespace,
            identity = expectedIdentity(context)
        )
        val executionScopes = addTargetSources(
            sources = sources,
            context = context,
            namespace = namespace,
            targets = targets,
            writeMetadata = writeMetadata,
            fullVisualSync = fullVisualSync
        )
        sources.forEach { (fileName, source) -> Files.writeString(outputDirectory.resolve(fileName), source) }
        val fileHashes = sources.mapValues { (_, source) ->
            Sha256Hash.of(source.toByteArray(StandardCharsets.UTF_8))
        }
        val manifestDraft = ExecutableRunnerManifest(
            path = outputDirectory.resolve(MANIFEST_FILE).toString(),
            schemaVersion = MANIFEST_SCHEMA_VERSION,
            mode = "official",
            entrypoint = "trunk-sync",
            target = targets.first(),
            targets = targets,
            writeMetadata = writeMetadata,
            transport = context.request.transport,
            namespace = namespace,
            sectionNodeId = null,
            roots = emptyList(),
            allowOfficialSections = false,
            fullVisualSync = fullVisualSync,
            allowPartial = false,
            metadataPageId = context.request.config.metadataPageId,
            modelPath = portablePath(context.request.toolsDirectory, context.request.modelPath),
            scriptPath = portablePath(context.request.toolsDirectory, context.request.scriptPath),
            modelHash = context.modelHash,
            gitSha = context.gitSha,
            designModelLength = context.modelJson.length,
            scriptLength = context.script.length,
            writerHash = context.writerHash,
            transportHash = context.transportHash,
            targetFingerprints = context.targetFingerprints,
            writerScopeFingerprints = context.writerScopeFingerprints,
            writerScopeFingerprintSchemaVersion = WriterScopeFingerprintCalculator.SCHEMA_VERSION,
            executionScopes = executionScopes,
            payloadImage = payloadImage,
            files = sources.keys.toList(),
            fileHashes = fileHashes,
            manifestHash = ""
        )
        return manifestJson.finalizeAndWrite(manifestDraft, outputDirectory.resolve(MANIFEST_FILE).toString())
    }

    private fun addChunkSources(
        sources: MutableMap<String, String>,
        context: Context,
        namespace: String
    ) {
        listOf("designModelJson" to context.modelJson, "script" to context.script).forEach { (key, value) ->
            val chunks = value.chunked(context.request.chunkSize).ifEmpty { listOf("") }
            var previousLength = 0
            chunks.forEachIndexed { index, chunk ->
                val prefix = if (key == "designModelJson") "10" else "20"
                val fileName = "$prefix-$key-${(index + 1).toString().padStart(3, '0')}.mcp.js"
                sources[fileName] = renderer.appendChunk(
                    metadataPageId = context.request.config.metadataPageId,
                    namespace = namespace,
                    key = key,
                    chunk = chunk,
                    chunkIndex = index + 1,
                    chunkCount = chunks.size,
                    previousLength = previousLength
                )
                previousLength += chunk.length
            }
        }
    }

    private fun addTargetSources(
        sources: MutableMap<String, String>,
        context: Context,
        namespace: String,
        targets: List<String>,
        writeMetadata: Boolean,
        fullVisualSync: Boolean
    ): Map<String, String> {
        val executionScopes = linkedMapOf<String, String>()
        if (!fullVisualSync) {
            val target = targets.single()
            val fileName = "99-run-target.mcp.js"
            sources[fileName] = targetSource(context, namespace, target, writeMetadata, target)
            executionScopes[fileName] = target
            return executionScopes
        }

        targets.forEachIndexed { index, target ->
            val roots = catalogRoots(context.designModel, target, context.request.config.catalogTargetNames)
            if (roots.isEmpty()) {
                val fileName = "99-${index.toString().padStart(2, '0')}-${safeName(target)}.mcp.js"
                sources[fileName] = targetSource(context, namespace, target, false, target)
                executionScopes[fileName] = target
            } else {
                roots.forEachIndexed { rootIndex, root ->
                    val scope = "$target.$root"
                    val fileName = "99-${index.toString().padStart(2, '0')}-" +
                        "${rootIndex.toString().padStart(2, '0')}-${safeName(target)}-${safeName(root)}.mcp.js"
                    sources[fileName] = targetSource(
                        context = context,
                        namespace = namespace,
                        target = target,
                        writeMetadata = false,
                        executionScope = scope,
                        roots = listOf(root)
                    )
                    executionScopes[fileName] = scope
                }
                val cleanupScope = "$target.cleanup"
                val cleanupFile = "99-${index.toString().padStart(2, '0')}-99-${safeName(target)}-cleanup.mcp.js"
                sources[cleanupFile] = targetSource(
                    context = context,
                    namespace = namespace,
                    target = target,
                    writeMetadata = false,
                    executionScope = cleanupScope,
                    cleanupOnly = true
                )
                executionScopes[cleanupFile] = cleanupScope
            }
        }
        return executionScopes
    }

    private fun targetSource(
        context: Context,
        namespace: String,
        target: String,
        writeMetadata: Boolean,
        executionScope: String,
        roots: List<String> = emptyList(),
        cleanupOnly: Boolean = false
    ): String {
        val executionMetadata = buildJsonObject {
            put("writerHash", context.writerHash)
            put("transportHash", context.transportHash)
            put("targetFingerprints", context.targetFingerprints.toJsonObject())
            put("writerScopeFingerprints", context.writerScopeFingerprints.toJsonObject())
            put("writerScopeFingerprintSchemaVersion", WriterScopeFingerprintCalculator.SCHEMA_VERSION)
        }
        val syncOptions = buildJsonObject {
            put("targets", JsonArray(listOf(JsonPrimitive(target))))
            put("writeMetadata", writeMetadata)
            if (roots.isNotEmpty()) {
                put("catalogRootFilters", buildJsonObject { put(target, JsonArray(roots.map(::JsonPrimitive))) })
            }
            if (cleanupOnly) put("catalogCleanupOnlyTargets", JsonArray(listOf(JsonPrimitive(target))))
            if (target.startsWith("ci.")) {
                val ciConfig = context.request.config.ciVisualPlanConfig
                    ?: throw IllegalArgumentException(
                        "CI visual target '$target' requires CI visual plan project configuration."
                    )
                put("ciVisualPlan", CiVisualPlanJson.create(context.designModel, ciConfig, target))
            }
            put("executionMetadata", executionMetadata)
        }
        return renderer.runTarget(
            metadataPageId = context.request.config.metadataPageId,
            namespace = namespace,
            syncOptions = syncOptions,
            executionScope = executionScope,
            modelTarget = target
        )
    }

    private fun expectedIdentity(context: Context): JsonObject = renderer.expectedIdentity(
        modelHash = context.modelHash,
        gitSha = context.gitSha,
        modelLength = context.modelJson.length,
        scriptLength = context.script.length,
        writerHash = context.writerHash,
        transportHash = context.transportHash
    )

    private fun transportHash(request: Request): String {
        val body = buildJsonObject {
            put("contractVersion", TRANSPORT_CONTRACT_VERSION)
            put("transport", request.transport)
            put("chunkSize", if (request.transport == TRANSPORT_CHUNKS) JsonPrimitive(request.chunkSize) else JsonNull)
            put(
                "payloadSchemaVersion",
                if (request.transport == TRANSPORT_PNG) {
                    JsonPrimitive(PayloadPngEncoder.PAYLOAD_SCHEMA_VERSION)
                } else {
                    JsonNull
                }
            )
            put("templates", renderer.templateHashes().toJsonObject())
        }
        return Sha256Hash.of(CanonicalJson.stringify(body))
    }

    private fun catalogRoots(
        designModel: JsonObject,
        target: String,
        catalogTargets: List<String>
    ): List<String> {
        if (target !in catalogTargets) return emptyList()
        val catalogName = target.substringBefore('.')
        val treeName = target.substringAfter('.')
        val nodes = designModel["content"]?.jsonObject
            ?.get("catalogs")?.jsonObject
            ?.get(catalogName)?.jsonObject
            ?.get(treeName)?.jsonArray
            ?: JsonArray(emptyList())
        val rootKey = if (treeName == "libraries") "group" else "id"
        return nodes.mapNotNull { node -> node.jsonObject[rootKey]?.jsonPrimitive?.contentOrNull }.distinct()
    }

    private fun validateModel(designModel: JsonObject) {
        require(designModel.requiredString("branch") == "main") {
            "MCP runners require a main design model. Found '${designModel.requiredString("branch")}'."
        }
        designModel.requiredString("gitSha")
        designModel.requiredString("modelHash")
    }

    private fun validateStagingEntry(key: String, value: String) {
        require(value.length <= MAX_SHARED_PLUGIN_DATA_ENTRY_LENGTH) {
            "$key is ${value.length} characters and exceeds the " +
                "$MAX_SHARED_PLUGIN_DATA_ENTRY_LENGTH-character sharedPluginData staging limit."
        }
    }

    private fun recreate(directory: Path) {
        if (Files.exists(directory)) {
            Files.walk(directory).use { paths ->
                paths.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists)
            }
        }
        Files.createDirectories(directory)
    }

    private fun portablePath(toolsDirectory: String, path: String): String {
        val tools = Path.of(toolsDirectory).toAbsolutePath().normalize()
        val target = Path.of(path).toAbsolutePath().normalize()
        return runCatching { tools.relativize(target).toString() }
            .getOrDefault(target.toString())
            .replace('\\', '/')
    }

    private fun safeName(value: String): String = value.replace(Regex("[^A-Za-z0-9_-]+"), "-")

    private fun JsonObject.requiredString(name: String): String =
        this[name]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Design model is missing '$name'.")

    private fun Map<String, String>.toJsonObject(): JsonObject =
        JsonObject(mapValues { (_, value) -> JsonPrimitive(value) })

    data class Request(
        val modelPath: String,
        val scriptPath: String,
        val outputDirectory: String,
        val toolsDirectory: String,
        val writerSourceDirectory: String,
        val repositoryRootDirectory: String,
        val changeImpactPolicyPath: String,
        val config: FigmaWriterRuntimeConfig,
        val transport: String = TRANSPORT_PNG,
        val chunkSize: Int = DEFAULT_CHUNK_SIZE
    )

    data class Result(
        val visualManifest: ExecutableRunnerManifest,
        val metadataManifest: ExecutableRunnerManifest
    )

    private data class Context(
        val request: Request,
        val designModel: JsonObject,
        val modelJson: String,
        val script: String,
        val modelHash: String,
        val gitSha: String,
        val writerHash: String,
        val transportHash: String,
        val targetFingerprints: Map<String, String>,
        val writerScopeFingerprints: Map<String, String>
    )

    companion object {
        const val TRANSPORT_PNG = "png"
        const val TRANSPORT_CHUNKS = "chunks"
        const val MANIFEST_SCHEMA_VERSION = 3

        private const val TRANSPORT_CONTRACT_VERSION = 2
        private const val DEFAULT_CHUNK_SIZE = 30_000
        private const val MINIMUM_CHUNK_SIZE = 1_000
        private const val MAX_SHARED_PLUGIN_DATA_ENTRY_LENGTH = 100_000
        private const val UTF8_BOM = "\uFEFF"
        private const val VISUAL_DIRECTORY = "visual"
        private const val METADATA_DIRECTORY = "metadata"
        private const val MANIFEST_FILE = "manifest.json"
        private const val PAYLOAD_PNG_FILE = "10-official-sync-payload.png"
        private const val CLEAR_STAGING_FILE = "00-clear-staging.mcp.js"
        private const val STAGE_PAYLOAD_FILE = "10-stage-payload-from-png.mcp.js"
        private const val FINALIZE_STAGING_FILE = "90-finalize-staging.mcp.js"

        private val SUPPORTED_TRANSPORTS = setOf(TRANSPORT_PNG, TRANSPORT_CHUNKS)
    }
}
