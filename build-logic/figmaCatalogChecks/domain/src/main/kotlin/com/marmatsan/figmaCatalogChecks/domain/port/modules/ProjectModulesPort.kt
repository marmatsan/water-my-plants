package com.marmatsan.figmaCatalogChecks.domain.port.modules

interface ProjectModulesPort {
    fun readModules(source: ProjectModulesSource): Set<String>
}
