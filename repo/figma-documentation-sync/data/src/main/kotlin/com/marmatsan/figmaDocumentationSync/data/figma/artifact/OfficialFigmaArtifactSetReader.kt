package com.marmatsan.figmaDocumentationSync.data.figma.artifact

import com.marmatsan.figmaDocumentationSync.domain.model.artifact.OfficialFigmaArtifactContract
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.tatarka.inject.annotations.Inject

/** Reads the JSON files and filesystem locations that make up an official artifact set. */
@Inject
class OfficialFigmaArtifactSetReader {
    fun read(
        artifactDirectory: String
    ): Result {
        val root = Path.of(artifactDirectory).toAbsolutePath().normalize()
        require(Files.isDirectory(root)) { "Artifact directory does not exist: '$root'." }

        val modelPath = findSingle(
            root = root,
            fileName = "design-model.json",
            description = "design model"
        )
        val scopePath = findSingle(
            root = root,
            fileName = "sync-scope.json",
            description = "sync scope"
        )
        val planPath = findSingle(
            root = root,
            fileName = "visual-sync-plan.json",
            description = "visual sync plan"
        )
        val manifestPaths = findAll(
            root = root,
            fileName = "manifest.json"
        )

        val modelJson = readJson(
            path = modelPath
        )
        val scopeJson = readJson(
            path = scopePath
        )
        val planJson = readJson(
            path = planPath
        )
        val manifests = manifestPaths.map { path -> path to readManifest(
            path = path
        ) }
        val visualManifestPaths = manifests.filter { it.second.fullVisualSync }.map { it.first }
        val metadataManifestPaths = manifests.filter { it.second.writeMetadata }.map { it.first }

        return Result(
            artifactDirectory = root,
            modelPath = modelPath,
            scopePath = scopePath,
            planPath = planPath,
            visualManifestPath = visualManifestPaths.singleOrNull(),
            metadataManifestPath = metadataManifestPaths.singleOrNull(),
            contract = OfficialFigmaArtifactContract(
                model = modelJson.toModel(),
                scope = scopeJson.toScope(),
                plan = planJson.toPlan(),
                manifests = manifests.map { it.second }
            )
        )
    }

    private fun JsonObject.toModel() = OfficialFigmaArtifactContract.Model(
        branch = requiredString(
            name = "branch",
            context = "design-model.json"
        ),
        gitSha = requiredString(
            name = "gitSha",
            context = "design-model.json"
        ),
        modelHash = requiredString(
            name = "modelHash",
            context = "design-model.json"
        )
    )

    private fun JsonObject.toScope() = OfficialFigmaArtifactContract.Scope(
        scope = requiredString(
            name = "scope",
            context = "sync-scope.json"
        ),
        gitSha = requiredString(
            name = "gitSha",
            context = "sync-scope.json"
        ),
        modelHash = requiredString(
            name = "modelHash",
            context = "sync-scope.json"
        ),
        writerHash = requiredString(
            name = "writerHash",
            context = "sync-scope.json"
        ),
        transportHash = requiredString(
            name = "transportHash",
            context = "sync-scope.json"
        ),
        visualRunnerManifestHash = requiredString(
            name = "visualRunnerManifestHash",
            context = "sync-scope.json"
        ),
        metadataRunnerManifestHash = requiredString(
            name = "metadataRunnerManifestHash",
            context = "sync-scope.json"
        ),
        visualSyncDecision = requiredString(
            name = "visualSyncDecision",
            context = "sync-scope.json"
        )
    )

    private fun JsonObject.toPlan(): OfficialFigmaArtifactContract.Plan {
        val identity = requiredObject(
            name = "identity",
            context = "visual-sync-plan.json"
        )
        return OfficialFigmaArtifactContract.Plan(
            decision = requiredString(
                name = "decision",
                context = "visual-sync-plan.json"
            ),
            manifestHash = requiredString(
                name = "manifestHash",
                context = "visual-sync-plan.json"
            ),
            identity = OfficialFigmaArtifactContract.Identity(
                modelHash = identity.requiredString(
                    "modelHash",
                    "visual-sync-plan.json identity"
                ),
                writerHash = identity.requiredString(
                    "writerHash",
                    "visual-sync-plan.json identity"
                ),
                transportHash = identity.requiredString(
                    "transportHash",
                    "visual-sync-plan.json identity"
                )
            )
        )
    }

    private fun readManifest(
        path: Path
    ): OfficialFigmaArtifactContract.Manifest {
        val json = readJson(
            path = path
        )
        val context = "${path.parent.fileName} manifest"
        return OfficialFigmaArtifactContract.Manifest(
            mode = json.requiredString(
                "mode",
                context
            ),
            gitSha = json.requiredString(
                "gitSha",
                context
            ),
            modelHash = json.requiredString(
                "modelHash",
                context
            ),
            manifestHash = json.requiredString(
                "manifestHash",
                context
            ),
            writerHash = json.requiredString(
                "writerHash",
                context
            ),
            transportHash = json.requiredString(
                "transportHash",
                context
            ),
            fullVisualSync = json.requiredBoolean(
                "fullVisualSync",
                context
            ),
            writeMetadata = json.requiredBoolean(
                "writeMetadata",
                context
            )
        )
    }

    private fun readJson(
        path: Path
    ): JsonObject = try {
        Json.parseToJsonElement(Files.readString(path).removePrefix(UTF8_BOM)).jsonObject
    } catch (
        exception: Exception
    ) {
        throw IllegalArgumentException(
            "Artifact '$path' is not valid JSON: ${exception.message}",
            exception
        )
    }

    private fun findSingle(
        root: Path,
        fileName: String,
        description: String
    ): Path {
        val paths = findAll(
            root = root,
            fileName = fileName
        )
        require(paths.size == 1) {
            "Expected exactly one $description under '$root'; found ${paths.size}."
        }
        return paths.single()
    }

    private fun findAll(
        root: Path,
        fileName: String
    ): List<Path> =
        Files.walk(root).use { paths ->
            paths.filter { path -> path.isRegularFile() && path.fileName.toString() == fileName }
                .sorted()
                .toList()
        }

    private fun JsonObject.requiredString(
        name: String,
        context: String
    ): String =
        this[name]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("$context is missing required property '$name'.")

    private fun JsonObject.requiredBoolean(
        name: String,
        context: String
    ): Boolean =
        this[name]?.jsonPrimitive?.boolean
            ?: throw IllegalArgumentException("$context is missing required property '$name'.")

    private fun JsonObject.requiredObject(
        name: String,
        context: String
    ): JsonObject =
        this[name]?.jsonObject
            ?: throw IllegalArgumentException("$context is missing required property '$name'.")

    data class Result(
        val artifactDirectory: Path,
        val modelPath: Path,
        val scopePath: Path,
        val planPath: Path,
        val visualManifestPath: Path?,
        val metadataManifestPath: Path?,
        val contract: OfficialFigmaArtifactContract
    )

    private companion object {
        const val UTF8_BOM = "\uFEFF"
    }
}
