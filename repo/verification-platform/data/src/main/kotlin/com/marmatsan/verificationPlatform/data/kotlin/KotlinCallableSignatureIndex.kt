package com.marmatsan.verificationPlatform.data.kotlin

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.About
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import java.io.File

/** Repository Kotlin callables whose source parameter names are stable and available. */
internal class KotlinCallableSignatureIndex private constructor(
    private val signaturesByName: Map<String, List<KotlinCallableSignature>>,
    private val functionValues: Set<KotlinFunctionValue>,
) {
    /**
     * Resolves the parameter name of every positional argument when all viable
     * repository declarations agree on the mapping.
     */
    fun positionalParameterNames(
        sourceIdentifier: String,
        callableName: String,
        arguments: List<KotlinCallArgument>,
    ): List<String?>? {
        if (KotlinFunctionValue(
                sourceIdentifier = sourceIdentifier,
                callableName = callableName,
            ) in functionValues
        ) {
            return null
        }
        val mappings =
            signaturesByName[callableName]
                .orEmpty()
                .filter { signature -> signature.sourceIdentifier == sourceIdentifier }
                .mapNotNull { signature ->
                    signature.map(
                        arguments = arguments,
                    )
                }.distinct()
        return mappings.singleOrNull()
    }

    internal companion object {
        /** Empty index used when the repository has no Kotlin source files. */
        val EMPTY =
            KotlinCallableSignatureIndex(
                signaturesByName = emptyMap(),
                functionValues = emptySet(),
            )

        /** Builds a deterministic callable index from the supplied Kotlin [sourceFiles]. */
        fun from(
            sourceFiles: List<File>,
        ): KotlinCallableSignatureIndex {
            if (sourceFiles.isEmpty()) {
                return EMPTY
            }
            val signatures = mutableSetOf<KotlinCallableSignature>()
            val functionValues = mutableSetOf<KotlinFunctionValue>()
            val engine =
                KtLintRuleEngine(
                    ruleProviders =
                        setOf(
                            RuleProvider {
                                KotlinCallableSignatureCollectorRule(
                                    signatures = signatures,
                                    functionValues = functionValues,
                                )
                            },
                        ),
                )
            sourceFiles.forEach { sourceFile ->
                engine.lint(Code.fromFile(sourceFile)) {
                    AutocorrectDecision.NO_AUTOCORRECT
                }
            }
            return KotlinCallableSignatureIndex(
                signaturesByName = signatures.groupBy(KotlinCallableSignature::callableName),
                functionValues = functionValues,
            )
        }
    }
}

/**
 * One call argument used to map positional values to source parameter names.
 *
 * @property argumentName explicit source name, or `null` when the argument is positional.
 */
internal data class KotlinCallArgument(
    val argumentName: String?,
)

private data class KotlinCallableSignature(
    val sourceIdentifier: String,
    val callableName: String,
    val parameterNames: List<String>,
    val requiredParameterNames: Set<String>,
    val varargParameterIndex: Int?,
) {
    fun map(
        arguments: List<KotlinCallArgument>,
    ): List<String?>? {
        if (arguments.size > parameterNames.size && varargParameterIndex == null) {
            return null
        }
        val mappedNames = mutableListOf<String?>()
        val assignedNames = mutableSetOf<String>()
        var positionalIndex = 0
        arguments.forEach { argument ->
            val existingName = argument.argumentName
            if (existingName != null) {
                if (existingName !in parameterNames ||
                    !assignedNames.add(
                        element = existingName,
                    )
                ) {
                    return null
                }
                mappedNames += null
                return@forEach
            }
            val parameterIndex =
                positionalIndex.coerceAtMost(
                    parameterNames.lastIndex,
                )
            if (
                parameterIndex < 0 ||
                (
                    varargParameterIndex != null &&
                        positionalIndex >= varargParameterIndex
                )
            ) {
                return null
            }
            val parameterName = parameterNames[parameterIndex]
            if (!assignedNames.add(
                    element = parameterName,
                )
            ) {
                return null
            }
            mappedNames += parameterName
            positionalIndex += 1
        }
        return mappedNames.takeIf {
            assignedNames.containsAll(requiredParameterNames)
        }
    }
}

private data class KotlinFunctionValue(
    val sourceIdentifier: String,
    val callableName: String,
)

private class KotlinCallableSignatureCollectorRule(
    private val signatures: MutableSet<KotlinCallableSignature>,
    private val functionValues: MutableSet<KotlinFunctionValue>,
) : Rule(
        ruleId = RuleId("repository-verification:kotlin-callable-signature-collector"),
        about =
            About(
                maintainer = "Repository Verification",
                repositoryUrl = "https://github.com/marmatsan/water-my-plants",
                issueTrackerUrl = "https://github.com/marmatsan/water-my-plants/issues",
            ),
    ),
    RuleAutocorrectApproveHandler {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        when (val declaration = node.psi) {
            is KtNamedFunction -> {
                declaration.name?.let { callableName ->
                    signatures +=
                        declaration.valueParameters.toSignature(
                            sourceIdentifier = declaration.sourceIdentifier(),
                            callableName = callableName,
                        )
                }
            }

            is KtClass -> {
                declaration.name?.let { callableName ->
                    signatures +=
                        declaration.primaryConstructorParameters.toSignature(
                            sourceIdentifier = declaration.sourceIdentifier(),
                            callableName = callableName,
                        )
                }
            }

            is KtSecondaryConstructor -> {
                generateSequence(declaration.parent) { parent -> parent.parent }
                    .filterIsInstance<KtClass>()
                    .firstOrNull()
                    ?.name
                    ?.let { callableName ->
                        signatures +=
                            declaration.valueParameters.toSignature(
                                sourceIdentifier = declaration.sourceIdentifier(),
                                callableName = callableName,
                            )
                    }
            }

            is KtParameter -> {
                if (declaration.typeReference?.text.isFunctionType()) {
                    declaration.name?.let { callableName ->
                        functionValues +=
                            KotlinFunctionValue(
                                sourceIdentifier = declaration.sourceIdentifier(),
                                callableName = callableName,
                            )
                    }
                }
            }

            is KtProperty -> {
                if (
                    declaration.typeReference?.text.isFunctionType() ||
                    declaration.initializer?.text?.trimStart()?.startsWith(
                        prefix = "{",
                    ) == true
                ) {
                    declaration.name?.let { callableName ->
                        functionValues +=
                            KotlinFunctionValue(
                                sourceIdentifier = declaration.sourceIdentifier(),
                                callableName = callableName,
                            )
                    }
                }
            }
        }
    }

    private fun List<KtParameter>.toSignature(
        sourceIdentifier: String,
        callableName: String,
    ): KotlinCallableSignature =
        KotlinCallableSignature(
            sourceIdentifier = sourceIdentifier,
            callableName = callableName,
            parameterNames = mapNotNull(KtParameter::getName),
            requiredParameterNames =
                filter { parameter ->
                    parameter.defaultValue == null && !parameter.hasModifier(KtTokens.VARARG_KEYWORD)
                }.mapNotNullTo(
                    mutableSetOf(),
                    KtParameter::getName,
                ),
            varargParameterIndex =
                indexOfFirst { parameter ->
                    parameter.hasModifier(KtTokens.VARARG_KEYWORD)
                }.takeIf { index -> index >= 0 },
        )

    private fun String?.isFunctionType(): Boolean = this?.contains("->") == true

    private fun org.jetbrains.kotlin.psi.KtElement.sourceIdentifier(): String =
        "${containingKtFile.packageFqName.asString()}/${containingKtFile.name}"
}
