package com.marmatsan.figmaDocumentationSync.data.fingerprint

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.CanonicalJson
import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaChangeImpactPolicy
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Computes target-scoped hashes for portable writer source files. */
class WriterScopeFingerprintCalculator {
    fun create(
        sourceRoot: Path,
        repositoryRoot: Path,
        policy: FigmaChangeImpactPolicy,
        writerTargets: List<String>,
        catalogTargets: List<String>,
        scopes: List<String>
    ): Map<String, String> {
        validateRuleTargets(policy, writerTargets)
        require(Files.isDirectory(sourceRoot)) { "Figma writer source root does not exist: $sourceRoot" }
        val sourceFiles = Files.walk(repositoryRoot).use { paths ->
            paths.filter(Path::isRegularFile)
                .sorted()
                .filter { source ->
                    val path = repositoryPath(repositoryRoot, source)
                    matchesAny(path, policy.visualWriterPaths) &&
                        !matchesAny(path, policy.transportOnlyPaths)
                }
                .toList()
        }
        require(sourceFiles.isNotEmpty()) {
            "No Figma writer sources matched the visual writer policy under $repositoryRoot."
        }

        val sourcesByTarget = writerTargets.associateWith { mutableListOf<SourceFingerprint>() }
        sourceFiles.forEach { source ->
            val path = repositoryPath(repositoryRoot, source)
            val matchedTargets = policy.visualTargetRules
                .filter { rule -> matchesAny(path, rule.paths) }
                .flatMap { rule -> rule.targets }
                .distinct()
            val affectedTargets = matchedTargets.ifEmpty { writerTargets }
            affectedTargets.forEach { target ->
                sourcesByTarget.getValue(target) += SourceFingerprint(
                    path = path,
                    sourceHash = Sha256Hash.of(Files.readAllBytes(source))
                )
            }
        }

        val targetFingerprints = writerTargets.associateWith { target ->
            val sourceJson = sourcesByTarget.getValue(target)
                .sortedBy(SourceFingerprint::path)
                .map { source ->
                    buildJsonObject {
                        put("path", source.path)
                        put("sourceHash", source.sourceHash)
                    }
                }
            val body = buildJsonObject {
                put("schemaVersion", SCHEMA_VERSION)
                put("target", target)
                put("sources", JsonArray(sourceJson))
            }
            Sha256Hash.of(CanonicalJson.stringify(body))
        }

        return (scopes + "metadata").distinct().associateWith { scope ->
            targetFingerprints.getValue(targetForScope(scope, writerTargets, catalogTargets))
        }
    }

    private fun validateRuleTargets(policy: FigmaChangeImpactPolicy, writerTargets: List<String>) {
        val unknownTargets = policy.visualTargetRules
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
        return catalogTargets.sortedByDescending(String::length)
            .firstOrNull { target -> scope.startsWith("$target.") }
            ?: throw IllegalArgumentException("Unknown Figma writer execution scope '$scope'.")
    }

    private fun repositoryPath(repositoryRoot: Path, source: Path): String =
        repositoryRoot.relativize(source).toString().replace('\\', '/')

    private fun matchesAny(path: String, patterns: List<String>): Boolean =
        patterns.any { pattern -> globRegex(pattern).matches(path) }

    private fun globRegex(pattern: String): Regex = Regex(
        buildString {
            append('^')
            pattern.replace('\\', '/').forEach { character ->
                when (character) {
                    '*' -> append(".*")
                    '?' -> append('.')
                    else -> append(Regex.escape(character.toString()))
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

    companion object {
        const val SCHEMA_VERSION = 1
    }
}
