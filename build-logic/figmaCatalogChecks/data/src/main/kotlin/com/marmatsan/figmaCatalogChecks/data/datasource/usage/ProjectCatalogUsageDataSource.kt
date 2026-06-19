package com.marmatsan.figmaCatalogChecks.data.datasource.usage

import com.marmatsan.figmaCatalogChecks.data.gradle.usage.GradleProjectCatalogUsageReader
import com.marmatsan.figmaCatalogChecks.domain.model.usage.ProjectCatalogUsage
import com.marmatsan.figmaCatalogChecks.domain.port.usage.ProjectCatalogUsagePort
import com.marmatsan.figmaCatalogChecks.domain.port.usage.ProjectCatalogUsageSource
import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
class ProjectCatalogUsageDataSource(
    private val gradleProjectCatalogUsageReader: GradleProjectCatalogUsageReader
) : ProjectCatalogUsagePort {
    override fun readProjectUsage(source: ProjectCatalogUsageSource): ProjectCatalogUsage =
        gradleProjectCatalogUsageReader.read(File(source.rootDirPath))
}
