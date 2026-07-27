package com.marmatsan.verificationPlatform.domain.service.documentation

internal class DocumentationFindings(
    val errors: MutableList<String> = mutableListOf(),
    val warnings: MutableList<String> = mutableListOf(),
)
