package com.marmatsan.verificationPlatform.domain.model

/**
 * Gradle module identity and its repository-relative source directory.
 *
 * @property id canonical Gradle project path such as `:core:ui`.
 * @property directory normalized repository-relative module directory.
 */
data class RepositoryModule(
    val id: String,
    val directory: String
)
