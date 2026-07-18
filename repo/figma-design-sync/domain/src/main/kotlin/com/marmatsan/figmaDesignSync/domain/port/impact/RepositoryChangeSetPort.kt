package com.marmatsan.figmaDesignSync.domain.port.impact

import com.marmatsan.figmaDesignSync.domain.model.impact.RepositoryChangeSet

/** Resolves repository-relative paths changed from the authoritative trunk. */
fun interface RepositoryChangeSetPort {
    fun read(repositoryRootPath: String): RepositoryChangeSet
}
