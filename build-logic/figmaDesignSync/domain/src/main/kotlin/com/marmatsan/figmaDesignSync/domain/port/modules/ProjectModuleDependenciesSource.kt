package com.marmatsan.figmaDesignSync.domain.port.modules

data class ProjectModuleDependenciesSource(
    val rootDirPath: String,
    val scope: ProjectModuleDependenciesScope
)
