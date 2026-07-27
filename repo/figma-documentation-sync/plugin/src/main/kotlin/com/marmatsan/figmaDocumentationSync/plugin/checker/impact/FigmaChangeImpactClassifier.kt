package com.marmatsan.figmaDocumentationSync.plugin.checker.impact

import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaChangeImpact
import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaChangeImpactPolicy
import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaImpact
import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaVerificationScope
import com.marmatsan.figmaDocumentationSync.domain.model.impact.RepositoryChangeSet
import me.tatarka.inject.annotations.Inject

/** Pure path classifier for repository changes that can affect Figma. */
@Inject
internal class FigmaChangeImpactClassifier {
    /** Classifies [changeSet] with [policy] into verification scope and affected visual targets. */
    fun classify(
        changeSet: RepositoryChangeSet,
        policy: FigmaChangeImpactPolicy,
    ): FigmaChangeImpact {
        val changedPaths =
            changeSet.changedPaths.map(
                transform = ::normalizePath,
            )
        val documentationOnly =
            changedPaths.isNotEmpty() &&
                changedPaths.all { path ->
                    matchesAny(
                        path = path,
                        patterns = policy.documentationOnlyPaths,
                    )
                }
        val transportOnly =
            !documentationOnly && changedPaths.isNotEmpty() &&
                changedPaths.all { path ->
                    matchesAny(
                        path = path,
                        patterns = policy.documentationOnlyPaths,
                    ) ||
                        matchesAny(
                            path = path,
                            patterns = policy.transportOnlyPaths,
                        )
                }
        val modelNeutralOnly =
            !documentationOnly && !transportOnly && changedPaths.isNotEmpty() &&
                changedPaths.all { path ->
                    matchesAny(
                        path = path,
                        patterns = policy.documentationOnlyPaths,
                    ) ||
                        matchesAny(
                            path = path,
                            patterns = policy.transportOnlyPaths,
                        ) ||
                        matchesAny(
                            path = path,
                            patterns = policy.modelNeutralPaths,
                        )
                }
        val modelContentChanged =
            changedPaths.any { path ->
                matchesAny(
                    path = path,
                    patterns = policy.modelContentPaths,
                )
            }
        val visualWriterPaths =
            changedPaths.filter { path ->
                matchesAny(
                    path = path,
                    patterns = policy.visualWriterPaths,
                )
            }
        val affectedVisualTargets =
            affectedVisualTargets(
                visualWriterPaths = visualWriterPaths,
                policy = policy,
            )

        val impact =
            when {
                documentationOnly -> FigmaImpact.DOCUMENTATION_ONLY
                transportOnly -> FigmaImpact.TRANSPORT_ONLY
                modelNeutralOnly -> FigmaImpact.MODEL_NEUTRAL
                modelContentChanged -> FigmaImpact.MODEL_CONTENT
                visualWriterPaths.isNotEmpty() -> FigmaImpact.VISUAL_TARGETS
                else -> FigmaImpact.UNKNOWN
            }
        val scope =
            when {
                documentationOnly -> FigmaVerificationScope.DOCUMENTATION_ONLY
                transportOnly -> FigmaVerificationScope.TRANSPORT_ONLY
                modelNeutralOnly -> FigmaVerificationScope.MODEL_NEUTRAL
                else -> FigmaVerificationScope.FULL_VERIFICATION
            }

        return FigmaChangeImpact(
            scope = scope,
            impact = impact,
            affectedVisualTargets = affectedVisualTargets,
            comparisonBase = changeSet.comparisonBase,
            changedPaths = changedPaths,
        )
    }

    private fun affectedVisualTargets(
        visualWriterPaths: List<String>,
        policy: FigmaChangeImpactPolicy,
    ): List<String> {
        val targets = linkedSetOf<String>()
        policy.visualTargetRules.forEach { rule ->
            if (visualWriterPaths.any { path ->
                    matchesAny(
                        path = path,
                        patterns = rule.paths,
                    )
                }
            ) {
                targets += rule.targets
            }
        }
        val hasUnmappedWriter =
            visualWriterPaths.any { path ->
                policy.visualTargetRules.none { rule ->
                    matchesAny(
                        path = path,
                        patterns = rule.paths,
                    )
                }
            }
        return if (hasUnmappedWriter) listOf(ALL_TARGETS) else targets.toList()
    }

    private fun matchesAny(
        path: String,
        patterns: List<String>,
    ): Boolean =
        patterns.any { pattern ->
            globRegex(
                pattern =
                    normalizePath(
                        path = pattern,
                    ),
            ).matches(path)
        }

    private fun globRegex(
        pattern: String,
    ): Regex =
        Regex(
            buildString {
                append('^')
                pattern.forEach { character ->
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
                                    literal = character.toString(),
                                ),
                            )
                        }
                    }
                }
                append('$')
            },
            RegexOption.IGNORE_CASE,
        )

    private fun normalizePath(
        path: String,
    ): String =
        path.trim().replace(
            '\\',
            '/',
        )

    private companion object {
        const val ALL_TARGETS = "all"
    }
}
