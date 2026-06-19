package com.marmatsan.figmaCatalogChecks.domain.port.modules

import com.marmatsan.figmaCatalogChecks.domain.model.modules.ModuleDependency

interface ProjectModuleDependenciesPort {
    fun readModuleDependencies(source: ProjectModuleDependenciesSource): Set<ModuleDependency>
}
