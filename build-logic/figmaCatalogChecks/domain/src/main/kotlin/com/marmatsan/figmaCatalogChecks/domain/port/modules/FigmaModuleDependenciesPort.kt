package com.marmatsan.figmaCatalogChecks.domain.port.modules

import com.marmatsan.figmaCatalogChecks.domain.model.modules.ModuleDependency

interface FigmaModuleDependenciesPort {
    fun readModuleDependencies(source: FigmaModuleDependenciesSource): Set<ModuleDependency>
}
