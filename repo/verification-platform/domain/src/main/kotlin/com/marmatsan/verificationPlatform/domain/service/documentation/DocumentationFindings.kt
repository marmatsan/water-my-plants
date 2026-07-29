package com.marmatsan.verificationPlatform.domain.service.documentation

/**
 * Mutable finding accumulator shared by focused documentation rules during one validation run.
 *
 * @property errors findings that make the documentation contract invalid.
 * @property warnings non-blocking findings that require maintainer attention.
 */
internal class DocumentationFindings(
    val errors: MutableList<String> = mutableListOf(),
    val warnings: MutableList<String> = mutableListOf()
)
