package com.marmatsan.waterMyPlants.projectConfig.catalog.configuration

import com.marmatsan.waterMyPlants.projectConfig.catalog.version.WaterMyPlantsVersionProperties
import org.gradle.api.initialization.Settings

/**
 * Registers repository test tooling separately from the production dependency catalog.
 *
 * @property versions Product version properties used by the auxiliary catalog.
 */
internal class WaterMyPlantsTestCatalogConfigurator(
    private val versions: WaterMyPlantsVersionProperties
) {
    /** Creates the consumer-owned `testLibs` catalog used by repository test conventions. */
    fun configure(
        settings: Settings
    ) {
        settings.dependencyResolutionManagement.versionCatalogs.create("testLibs") {
            library(
                "com.marmatsan.repo.unit.test.dsl",
                "com.marmatsan.repo",
                "unit-test-dsl"
            ).version(
                versions.required(
                    key = "unitTestDslLibraryVersion"
                )
            )
        }
    }
}
