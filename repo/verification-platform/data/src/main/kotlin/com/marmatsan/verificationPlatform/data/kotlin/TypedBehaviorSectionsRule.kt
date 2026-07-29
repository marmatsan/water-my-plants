package com.marmatsan.verificationPlatform.data.kotlin

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EOL_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/** Requires executable typed behavior phases instead of legacy section comments. */
internal class TypedBehaviorSectionsRule :
    Rule(
        ruleId = RuleId("repository-verification:typed-behavior-sections"),
        about = RepositoryKotlinRuleMetadata.about
    ),
    RuleAutocorrectApproveHandler {
    /** Reports a legacy behavior-section marker without attempting an unsafe rewrite. */
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision
    ) {
        if (node.elementType != EOL_COMMENT || !LEGACY_SECTION.matches(node.text.trim())) {
            return
        }
        emit(
            node.startOffset,
            "Use the typed given { }.whenever { }.then { } behavior chain instead of section comments.",
            false
        )
    }

    private companion object {
        val LEGACY_SECTION = Regex("(?i)//\\s*(given|when|then)\\b.*")
    }
}
