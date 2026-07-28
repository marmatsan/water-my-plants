package com.marmatsan.verificationPlatform.domain.service.errorhandling

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.marmatsan.verificationPlatform.domain.model.errorhandling.TypedResultUsageError
import com.marmatsan.verificationPlatform.domain.model.errorhandling.TypedResultUsageViolation

/** Validates that product Kotlin sources use one configured typed-result implementation. */
class TypedResultUsageValidator(
    private val acceptedResultQualifiedName: String,
) {
    private val codeMasker = KotlinSourceCodeMasker()

    init {
        require(acceptedResultQualifiedName.isNotBlank()) {
            "The accepted Result qualified name must not be blank."
        }
    }

    /**
     * Finds incompatible `Result` imports and declarations in [source].
     *
     * @param relativePath repository-relative path reported with each violation.
     * @param source Kotlin source text to inspect.
     */
    fun validate(
        relativePath: String,
        source: String,
    ): Result<Unit, TypedResultUsageError> {
        val violations =
            codeMasker
                .mask(
                    source = source,
                ).lineSequence()
                .flatMapIndexed { index, line ->
                    validateLine(
                        relativePath = relativePath,
                        lineNumber = index + 1,
                        line = line,
                    )
                }.toList()

        return if (violations.isEmpty()) {
            Ok(Unit)
        } else {
            Err(
                TypedResultUsageError(
                    violations = violations,
                ),
            )
        }
    }

    private fun validateLine(
        relativePath: String,
        lineNumber: Int,
        line: String,
    ): Sequence<TypedResultUsageViolation> {
        val trimmedLine = line.trim()
        if (
            trimmedLine.isEmpty() ||
            trimmedLine.startsWith("//") ||
            trimmedLine.startsWith("/*") ||
            trimmedLine.startsWith("*")
        ) {
            return emptySequence()
        }

        return buildList {
            RESULT_IMPORT.matchEntire(line)?.let { match ->
                val importedResult = match.groupValues[1]
                val alias = match.groupValues[2]
                if (importedResult != acceptedResultQualifiedName) {
                    add(
                        violation(
                            relativePath = relativePath,
                            lineNumber = lineNumber,
                            reason =
                                "must import $acceptedResultQualifiedName instead of " +
                                    importedResult,
                        ),
                    )
                } else if (alias.isNotEmpty()) {
                    add(
                        violation(
                            relativePath = relativePath,
                            lineNumber = lineNumber,
                            reason =
                                "must import $acceptedResultQualifiedName without an alias",
                        ),
                    )
                }
            }

            if (CUSTOM_RESULT_DECLARATION.containsMatchIn(line)) {
                add(
                    violation(
                        relativePath = relativePath,
                        lineNumber = lineNumber,
                        reason =
                            "must not declare a custom Result; use " +
                                acceptedResultQualifiedName,
                    ),
                )
            }

            if (!line.trimStart().startsWith("import ")) {
                QUALIFIED_RESULT_REFERENCE
                    .findAll(line)
                    .map { match -> match.groupValues[1] }
                    .filter { qualifiedName -> qualifiedName != acceptedResultQualifiedName }
                    .forEach { qualifiedName ->
                        add(
                            violation(
                                relativePath = relativePath,
                                lineNumber = lineNumber,
                                reason =
                                    "must use $acceptedResultQualifiedName instead of " +
                                        qualifiedName,
                            ),
                        )
                    }
            }
        }.distinct().asSequence()
    }

    private fun violation(
        relativePath: String,
        lineNumber: Int,
        reason: String,
    ): TypedResultUsageViolation =
        TypedResultUsageViolation(
            relativePath = relativePath,
            lineNumber = lineNumber,
            reason = reason,
        )

    private companion object {
        val RESULT_IMPORT =
            Regex(
                pattern =
                    """^\s*import\s+([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*\.Result)""" +
                        """(?:\s+as\s+([A-Za-z_]\w*))?\s*$""",
            )
        val CUSTOM_RESULT_DECLARATION =
            Regex(
                pattern =
                    """\b(?:class|interface)\s+Result\b|""" +
                        """\btypealias\s+Result\b""",
            )
        val QUALIFIED_RESULT_REFERENCE =
            Regex(
                pattern =
                    """\b([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)+\.Result)\b""",
            )
    }
}
