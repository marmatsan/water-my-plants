package com.marmatsan.figmaCatalogChecks.domain.usecase.modules

import com.marmatsan.figmaCatalogChecks.domain.port.modules.FigmaModuleComponentSource
import com.marmatsan.figmaCatalogChecks.domain.port.modules.FigmaModulesPort

internal class FakeFigmaModulesPort(
    private val modules: Set<String>
) : FigmaModulesPort {
    override fun readModules(source: FigmaModuleComponentSource): Set<String> = modules
}
