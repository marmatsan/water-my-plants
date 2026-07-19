package com.marmatsan.ci.domain.model

data class RepositoryChangeSet(
    val comparisonBase: String?,
    val head: String,
    val changedFiles: List<String>
)
