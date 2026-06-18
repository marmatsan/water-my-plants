package com.marmatsan.figmaCatalogChecks.domain.usecase.modules

import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModulesSource

internal class FakeProjectModulesPort(
    private val modules: Set<String>
) : ProjectModulesPort {
    override fun readModules(source: ProjectModulesSource): Set<String> = modules
}
