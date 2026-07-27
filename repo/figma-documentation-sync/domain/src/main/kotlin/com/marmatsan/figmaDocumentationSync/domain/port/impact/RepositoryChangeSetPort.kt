package com.marmatsan.figmaDocumentationSync.domain.port.impact

import com.marmatsan.figmaDocumentationSync.domain.model.impact.RepositoryChangeSet

/** Resolves repository-relative paths changed from the authoritative trunk. */
fun interface RepositoryChangeSetPort {
    /** Resolves the authoritative comparison base and changed paths for [repositoryRootPath]. */
    fun read(
        repositoryRootPath: String,
    ): RepositoryChangeSet
}
