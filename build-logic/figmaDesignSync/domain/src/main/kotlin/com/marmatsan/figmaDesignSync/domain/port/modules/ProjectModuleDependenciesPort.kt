package com.marmatsan.figmaDesignSync.domain.port.modules

import com.marmatsan.figmaDesignSync.domain.model.modules.ModuleDependency

interface ProjectModuleDependenciesPort {
    fun readModuleDependencies(source: ProjectModuleDependenciesSource): Set<ModuleDependency>
}
