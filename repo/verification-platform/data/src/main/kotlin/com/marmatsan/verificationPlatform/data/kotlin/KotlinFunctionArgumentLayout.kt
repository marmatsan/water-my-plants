package com.marmatsan.verificationPlatform.data.kotlin

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import java.io.File

/** Parses Kotlin source and applies the repository function-argument layout. */
class KotlinFunctionArgumentLayout(
    sourceFiles: List<File> = emptyList()
) {
    private val callableSignatures = KotlinCallableSignatureIndex.from(
        sourceFiles = sourceFiles
    )
    private val checkRuleEngine = KtLintRuleEngine(
        ruleProviders = setOf(
            RuleProvider {
            MultilineFunctionArgumentsRule(
                compactShortFunctionTypes = false,
                callableSignatures = callableSignatures
            )
        }
        )
    )
    private val formatRuleEngine = KtLintRuleEngine(
        ruleProviders = setOf(
            RuleProvider {
            MultilineFunctionArgumentsRule(
                compactShortFunctionTypes = true,
                callableSignatures = callableSignatures
            )
        }
        )
    )

    /** Returns every function parameter or call argument that violates the layout. */
    fun inspect(
        file: File
    ): List<Violation> = buildList {
        checkRuleEngine.lint(Code.fromFile(file)) { error ->
            add(
                element = Violation(
                    line = error.line,
                    column = error.col,
                    detail = error.detail
                )
            )
        }
    }

    /** Returns [file] formatted without changing it on disk. */
    fun format(
        file: File
    ): String = formatRuleEngine.format(
        code = Code.fromFile(file)
    ) {
        AutocorrectDecision.ALLOW_AUTOCORRECT
    }

    /**
     * One source location that violates the repository layout.
     *
     * @property line One-based source line.
     * @property column One-based source column.
     * @property detail Human-readable explanation of the violation.
     */
    data class Violation(
        val line: Int,
        val column: Int,
        val detail: String
    )
}
