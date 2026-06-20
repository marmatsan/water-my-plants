package com.marmatsan.figmaDesignSync.domain.model.versions

data class RepositoryVersionSection(
    val name: String,
    val versions: Map<String, String>
)
