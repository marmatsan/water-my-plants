package com.marmatsan.figmaDesignSync.domain.port.modules

interface ProjectModulesPort {
    fun readModules(source: ProjectModulesSource): Set<String>
}
