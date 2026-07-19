package com.marmatsan.figmaDocumentationSync.teamcityAdapter

/** TeamCity build identity required before consuming its published artifacts. */
data class TeamCityBuild(
    val id: Long,
    val state: String,
    val status: String,
    val branchName: String,
    val buildTypeName: String,
    val webUrl: String?
)
