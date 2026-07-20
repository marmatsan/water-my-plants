package com.marmatsan.verificationPlatform.data.kotlin

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.EditorConfigDefaults
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.propertyTypes
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import java.io.File

/**
 * Applies the canonical standard and repository-owned KtLint rules.
 *
 * @param sourceFiles Repository sources used to resolve supported named arguments.
 * @param editorConfigFile Canonical repository `.editorconfig` used by every source.
 */
class RepositoryKotlinStyle(
    sourceFiles: List<File> = emptyList(),
    editorConfigFile: File,
) {
    private val editorConfigFile =
        editorConfigFile.canonicalFile.also { file ->
            require(file.isFile) {
                "Repository EditorConfig does not exist: ${file.invariantSeparatorsPath}"
            }
        }
    private val callableSignatures =
        KotlinCallableSignatureIndex.from(
            sourceFiles = sourceFiles,
        )
    private val checkRuleEngine =
        createRuleEngine(
            compactShortFunctionTypes = false,
        )
    private val formatRuleEngine =
        createRuleEngine(
            compactShortFunctionTypes = true,
        )

    /** Returns every standard or repository-owned KtLint violation in [file]. */
    fun inspect(
        file: File,
    ): List<Violation> =
        buildList {
            checkRuleEngine.lint(Code.fromFile(file)) { error ->
                add(
                    element =
                        Violation(
                            line = error.line,
                            column = error.col,
                            ruleId = error.ruleId.value,
                            detail = error.detail,
                        ),
                )
            }
        }

    /** Returns [file] formatted without changing it on disk. */
    fun format(
        file: File,
    ): String =
        formatRuleEngine.format(
            code = Code.fromFile(file),
        ) {
            AutocorrectDecision.ALLOW_AUTOCORRECT
        }

    private fun createRuleEngine(
        compactShortFunctionTypes: Boolean,
    ): KtLintRuleEngine {
        val ruleProviders =
            StandardRuleSetProvider().getRuleProviders() +
                RuleProvider {
                    MultilineFunctionArgumentsRule(
                        compactShortFunctionTypes = compactShortFunctionTypes,
                        callableSignatures = callableSignatures,
                    )
                }
        return KtLintRuleEngine(
            ruleProviders = ruleProviders,
            editorConfigDefaults =
                EditorConfigDefaults.load(
                    path = editorConfigFile.toPath(),
                    propertyTypes = ruleProviders.propertyTypes(),
                ),
        )
    }

    /**
     * One source location that violates the canonical Kotlin style.
     *
     * @property line One-based source line.
     * @property column One-based source column.
     * @property ruleId Stable KtLint identifier for the violated rule.
     * @property detail Human-readable explanation of the violation.
     */
    data class Violation(
        val line: Int,
        val column: Int,
        val ruleId: String,
        val detail: String,
    )
}
