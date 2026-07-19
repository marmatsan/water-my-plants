package com.marmatsan.figmaDocumentationSync.domain.port.impact

import com.marmatsan.figmaDocumentationSync.domain.model.impact.RepositoryChangeSet

/** Resolves repository-relative paths changed from the authoritative trunk. */
fun interface RepositoryChangeSetPort {
    fun read(repositoryRootPath: String): RepositoryChangeSet
}
