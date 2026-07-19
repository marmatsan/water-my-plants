package com.marmatsan.ci.domain.model

/** Gradle module identity and its repository-relative source directory. */
data class RepositoryModule(
    val id: String,
    val directory: String
)
