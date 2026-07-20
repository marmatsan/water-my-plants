package com.marmatsan.verificationPlatform.data.kotlin

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUNCTION_TYPE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LPAR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RPAR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_NAME
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.About
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpace20
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpaceWithNewline20
import com.pinterest.ktlint.rule.engine.core.api.prevLeaf
import com.pinterest.ktlint.rule.engine.core.api.replaceTextWith
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList

/** Requires named repository calls and vertical function argument layout. */
internal class MultilineFunctionArgumentsRule(
    private val compactShortFunctionTypes: Boolean,
    private val callableSignatures: KotlinCallableSignatureIndex
) : Rule(
    ruleId = RuleId("water-my-plants:multiline-function-arguments"),
    about = About(
        maintainer = "Water My Plants",
        repositoryUrl = "https://github.com/marmatsan/water-my-plants",
        issueTrackerUrl = "https://github.com/marmatsan/water-my-plants/issues"
    )
), RuleAutocorrectApproveHandler {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision
    ) {
        val listKind = when (node.elementType) {
            VALUE_ARGUMENT_LIST -> ListKind.ARGUMENT
            VALUE_PARAMETER_LIST -> ListKind.PARAMETER
            else -> return
        }
        var children = node.children()
        var items = children.filter { child -> child.elementType == listKind.itemType }
        var closingParenthesis = children.lastOrNull { child -> child.elementType == RPAR }
        if (listKind == ListKind.ARGUMENT) {
            node.ensureNamedArguments(
                items = items,
                emit = emit
            )
            children = node.children()
            items = children.filter { child -> child.elementType == listKind.itemType }
            closingParenthesis = children.lastOrNull { child -> child.elementType == RPAR }
        }
        if (listKind == ListKind.PARAMETER && node.isFunctionTypeParameterList()) {
            val compactText = items.joinToString(
                prefix = "(",
                postfix = ")",
                separator = ", "
            ) { item -> item.text.normalizedInlineWhitespace() }
            if (node.lineLengthWith(
                replacement = compactText
            ) <= MAX_LINE_LENGTH) {
                if (
                    compactShortFunctionTypes &&
                    node.text.contains('\n') &&
                    !node.text.contains("//") &&
                    !node.text.contains("/*")
                ) {
                    emit(
                        node.startOffset,
                        "Function type parameters may remain inline within the repository line limit.",
                        true
                    ).ifAutocorrectAllowed {
                        node.compactFunctionTypeWhitespace(
                            items = items,
                            closingParenthesis = closingParenthesis
                        )
                    }
                }
                return
            }
        }
        if (
            !listKind.requiresMultiline(
                itemCount = items.size,
                hasNamedArgument = items.any { item -> item.isNamedArgument() },
                isAlreadyMultiline = node.text.contains('\n')
            ) ||
            children.none { child -> child.elementType == LPAR } ||
            closingParenthesis == null
        ) {
            return
        }

        val baseIndent = node.lineIndent()
        val itemIndent = baseIndent + INDENT
        items.forEach { item ->
            item.ensureNewlineBefore(
                indent = itemIndent,
                message = listKind.itemMessage,
                emit = emit
            )
        }
        closingParenthesis.ensureNewlineBefore(
            indent = baseIndent,
            message = "A multiline function parameter or argument list must close on a separate line.",
            emit = emit
        )
    }

    private fun ASTNode.ensureNamedArguments(
        items: List<ASTNode>,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision
    ) {
        val argumentList = psi as? KtValueArgumentList ?: return
        if (argumentList.containingFile.name.endsWith(".kts")) {
            return
        }
        val callExpression = argumentList.parent as? KtCallExpression ?: return
        if (
            callExpression.hasExplicitReceiver() ||
            callExpression.isInsideImplicitReceiverScope()
        ) {
            return
        }
        val callableName = callExpression.calleeExpression?.text ?: return
        if (!callableName.matches(KOTLIN_IDENTIFIER)) {
            return
        }
        val arguments = items.map { item ->
            KotlinCallArgument(
                argumentName = (item.psi as? KtValueArgument)
                    ?.getArgumentName()
                    ?.asName
                    ?.asString()
            )
        }
        val parameterNames = callableSignatures.positionalParameterNames(
            sourceIdentifier =
                "${argumentList.containingKtFile.packageFqName.asString()}/" +
                    argumentList.containingKtFile.name,
            callableName = callableName,
            arguments = arguments
        ) ?: return
        items.zip(parameterNames).forEach { (item, parameterName) ->
            if (parameterName == null || item.isNamedArgument()) {
                return@forEach
            }
            emit(
                item.startOffset,
                "Calls to repository Kotlin functions must use named arguments.",
                true
            ).ifAutocorrectAllowed {
                item.replaceWithNamedArgument(
                    parameterName = parameterName
                )
            }
        }
    }

    private fun KtCallExpression.hasExplicitReceiver(): Boolean =
        (parent as? KtQualifiedExpression)?.selectorExpression == this

    private fun KtCallExpression.isInsideImplicitReceiverScope(): Boolean {
        val enclosingLambda = generateSequence(parent) { element -> element.parent }
            .filterIsInstance<KtLambdaExpression>()
            .firstOrNull() ?: return false
        val lambdaArgument = enclosingLambda.parent as? KtLambdaArgument ?: return false
        val scopeCall = lambdaArgument.parent as? KtCallExpression ?: return false
        return scopeCall.calleeExpression?.text in IMPLICIT_RECEIVER_SCOPE_FUNCTIONS
    }

    private fun ASTNode.replaceWithNamedArgument(
        parameterName: String
    ) {
        val argument = psi as? KtValueArgument ?: return
        val replacement = KtPsiFactory(
            project = argument.project,
            markGenerated = false
        ).createArgument("$parameterName = ${argument.text}")
        treeParent?.replaceChild(
            this,
            replacement.node.clone() as ASTNode
        )
    }

    private fun ASTNode.ensureNewlineBefore(
        indent: String,
        message: String,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision
    ) {
        val previousLeaf = prevLeaf
        if (
            previousLeaf?.isWhiteSpaceWithNewline20 == true &&
            previousLeaf.text.substringAfterLast('\n') == indent
        ) {
            return
        }

        emit(
            startOffset,
            message,
            true
        ).ifAutocorrectAllowed {
            val whitespace = if (previousLeaf?.isWhiteSpaceWithNewline20 == true) {
                previousLeaf.text.substringBeforeLast('\n') + "\n$indent"
            } else {
                "\n$indent"
            }
            if (previousLeaf?.isWhiteSpace20 == true) {
                previousLeaf.replaceTextWith(whitespace)
            } else {
                treeParent?.addChild(
                    PsiWhiteSpaceImpl(whitespace),
                    this
                )
            }
        }
    }

    private fun ASTNode.compactFunctionTypeWhitespace(
        items: List<ASTNode>,
        closingParenthesis: ASTNode?
    ) {
        items.forEachIndexed { index, item ->
            item.prevLeaf
                ?.takeIf { leaf -> leaf.isWhiteSpace20 }
                ?.replaceOrRemoveWhitespace(
                    replacement = if (index == 0) "" else " "
                )
        }
        closingParenthesis
            ?.prevLeaf
            ?.takeIf { leaf -> leaf.isWhiteSpace20 }
            ?.replaceOrRemoveWhitespace(
                replacement = ""
            )
    }

    private fun ASTNode.replaceOrRemoveWhitespace(
        replacement: String
    ) {
        if (replacement.isEmpty()) {
            treeParent?.removeChild(this)
        } else {
            replaceTextWith(replacement)
        }
    }

    private fun ASTNode.lineIndent(): String {
        val source = psi.containingFile.text
        val safeOffset = startOffset.coerceIn(
            0,
            source.length
        )
        val lineStart = source.lastIndexOf(
            '\n',
            safeOffset - 1
        ) + 1
        return source.substring(
            lineStart,
            safeOffset
        ).takeWhile { character -> character == ' ' || character == '\t' }
    }

    private fun ASTNode.lineLengthWith(
        replacement: String
    ): Int {
        val source = psi.containingFile.text
        val safeStartOffset = startOffset.coerceIn(
            0,
            source.length
        )
        val safeEndOffset = (startOffset + textLength).coerceIn(
            safeStartOffset,
            source.length
        )
        val lineStart = source.lastIndexOf(
            '\n',
            safeStartOffset - 1
        ) + 1
        val lineEnd = source.indexOf(
            '\n',
            safeEndOffset
        ).let { index -> if (index == -1) source.length else index }
        return source.substring(
            lineStart,
            safeStartOffset
        ).length + replacement.length + source.substring(
            safeEndOffset,
            lineEnd
        ).length
    }

    private fun ASTNode.children(): List<ASTNode> = buildList {
        var child = firstChildNode
        while (child != null) {
            add(
                element = child
            )
            child = child.treeNext
        }
    }

    private fun ASTNode.isFunctionTypeParameterList(): Boolean =
        generateSequence(treeParent) { parent -> parent.treeParent }
            .any { parent -> parent.elementType == FUNCTION_TYPE }

    private fun String.normalizedInlineWhitespace(): String =
        replace(
            Regex("\\s+"),
            " "
        ).trim()

    private companion object {
        const val MINIMUM_MULTILINE_ARGUMENT_COUNT = 2
        const val MAX_LINE_LENGTH = 120
        const val INDENT = "    "
        val KOTLIN_IDENTIFIER = Regex("[A-Za-z_][A-Za-z0-9_]*")
        val IMPLICIT_RECEIVER_SCOPE_FUNCTIONS = setOf(
            "apply",
            "run",
            "with"
        )
    }

    private enum class ListKind(
        val itemType: org.jetbrains.kotlin.com.intellij.psi.tree.IElementType,
        val itemMessage: String
    ) {
        PARAMETER(
            itemType = VALUE_PARAMETER,
            itemMessage = "Every function parameter must start on a separate line."
        ),
        ARGUMENT(
            itemType = VALUE_ARGUMENT,
            itemMessage = "Function arguments must each start on a separate line."
        );

        fun requiresMultiline(
            itemCount: Int,
            hasNamedArgument: Boolean,
            isAlreadyMultiline: Boolean
        ): Boolean = when (this) {
            PARAMETER -> itemCount > 0
            ARGUMENT ->
                itemCount >= MINIMUM_MULTILINE_ARGUMENT_COUNT ||
                    hasNamedArgument ||
                    isAlreadyMultiline
        }
    }

    private fun ASTNode.isNamedArgument(): Boolean =
        children().any { child -> child.elementType == VALUE_ARGUMENT_NAME }
}
