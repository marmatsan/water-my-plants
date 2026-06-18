package com.marmatsan.figmaCatalogChecks.domain.port.modules

interface FigmaModulesPort {
    fun readModules(source: FigmaModuleComponentSource): Set<String>
}
