package com.marmatsan.figmaDocumentationSync.data.fingerprint

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.CanonicalJson
import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaChangeImpactPolicy
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

/** Computes target-scoped hashes for portable writer source files. */
class WriterScopeFingerprintCalculator {
    /** Creates deterministic writer fingerprints for model-neutral and visual execution scopes. */
    fun create(
        sourceRoot: Path,
        repositoryRoot: Path,
        policy: FigmaChangeImpactPolicy,
        writerTargets: List<String>,
        catalogTargets: List<String>,
        scopes: List<String>
    ): Map<String, String> {
        validateRuleTargets(
            policy = policy,
            writerTargets = writerTargets
        )
        require(Files.isDirectory(sourceRoot)) { "Figma writer source root does not exist: $sourceRoot" }
        val sourceFiles =
            Files.walk(repositoryRoot).use { paths ->
                paths
                    .filter(Path::isRegularFile)
                    .sorted()
                    .filter { source ->
                        val path =
                            repositoryPath(
                                repositoryRoot = repositoryRoot,
                                source = source
                            )
                        matchesAny(
                            path = path,
                            patterns = policy.visualWriterPaths
                        ) &&
                            !matchesAny(
                                path = path,
                                patterns = policy.transportOnlyPaths
                            )
                    }.toList()
            }
        require(sourceFiles.isNotEmpty()) {
            "No Figma writer sources matched the visual writer policy under $repositoryRoot."
        }

        val sourcesByTarget = writerTargets.associateWith { mutableListOf<SourceFingerprint>() }
        sourceFiles.forEach { source ->
            val path =
                repositoryPath(
                    repositoryRoot = repositoryRoot,
                    source = source
                )
            val matchedTargets =
                policy.visualTargetRules
                    .filter { rule ->
                        matchesAny(
                            path = path,
                            patterns = rule.paths
                        )
                    }.flatMap { rule -> rule.targets }
                    .distinct()
            val affectedTargets = matchedTargets.ifEmpty { writerTargets }
            affectedTargets.forEach { target ->
                sourcesByTarget.getValue(target) +=
                    SourceFingerprint(
                        path = path,
                        sourceHash =
                            Sha256Hash.of(
                                value = Files.readAllBytes(source)
                            )
                    )
            }
        }

        val targetFingerprints =
            writerTargets.associateWith { target ->
                val sourceJson =
                    sourcesByTarget
                        .getValue(target)
                        .sortedBy(SourceFingerprint::path)
                        .map { source ->
                            buildJsonObject {
                                put(
                                    "path",
                                    source.path
                                )
                                put(
                                    "sourceHash",
                                    source.sourceHash
                                )
                            }
                        }
                val body =
                    buildJsonObject {
                        put(
                            "schemaVersion",
                            SCHEMA_VERSION
                        )
                        put(
                            "target",
                            target
                        )
                        put(
                            "sources",
                            JsonArray(sourceJson)
                        )
                    }
                Sha256Hash.of(
                    value =
                        CanonicalJson.stringify(
                            value = body
                        )
                )
            }

        return (scopes + "metadata").distinct().associateWith { scope ->
            targetFingerprints.getValue(
                targetForScope(
                    scope = scope,
                    writerTargets = writerTargets,
                    catalogTargets = catalogTargets
                )
            )
        }
    }

    private fun validateRuleTargets(
        policy: FigmaChangeImpactPolicy,
        writerTargets: List<String>
    ) {
        val unknownTargets =
            policy.visualTargetRules
                .flatMap { rule -> rule.targets }
                .distinct()
                .filterNot(writerTargets::contains)
        require(unknownTargets.isEmpty()) {
            "Unknown Figma writer target(s) in change-impact policy: ${unknownTargets.joinToString(", ")}."
        }
    }

    private fun targetForScope(
        scope: String,
        writerTargets: List<String>,
        catalogTargets: List<String>
    ): String {
        if (scope in writerTargets) return scope
        return catalogTargets
            .sortedByDescending(String::length)
            .firstOrNull { target ->
                scope.startsWith(
                    prefix = "$target."
                )
            }
            ?: throw IllegalArgumentException("Unknown Figma writer execution scope '$scope'.")
    }

    private fun repositoryPath(
        repositoryRoot: Path,
        source: Path
    ): String =
        repositoryRoot.relativize(source).toString().replace(
            '\\',
            '/'
        )

    private fun matchesAny(
        path: String,
        patterns: List<String>
    ): Boolean =
        patterns.any { pattern ->
            globRegex(
                pattern = pattern
            ).matches(path)
        }

    private fun globRegex(
        pattern: String
    ): Regex =
        Regex(
            buildString {
                append('^')
                pattern
                    .replace(
                        '\\',
                        '/'
                    ).forEach { character ->
                        when (character) {
                            '*' -> {
                                append(".*")
                            }

                            '?' -> {
                                append('.')
                            }

                            else -> {
                                append(
                                    Regex.escape(
                                        literal = character.toString()
                                    )
                                )
                            }
                        }
                    }
                append('$')
            },
            RegexOption.IGNORE_CASE
        )

    private data class SourceFingerprint(
        val path: String,
        val sourceHash: String
    )

    /** Fingerprint contract version shared with canonical metadata. */
    companion object {
        /** Current writer-scope fingerprint calculation schema version. */
        const val SCHEMA_VERSION = 1
    }
}
