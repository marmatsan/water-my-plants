package com.marmatsan.waterMyPlants.projectConfig.catalog.configuration

import org.gradle.api.initialization.Settings
import java.io.File
import java.util.Properties

/** Registers repository test tooling separately from the production dependency catalog. */
internal class WaterMyPlantsTestCatalogConfigurator {
    /** Creates the consumer-owned `testLibs` catalog used by repository test conventions. */
    fun configure(
        settings: Settings,
    ) {
        val versionsFile =
            findVersionsFile(
                rootDirectory = settings.rootDir,
            )
        val versions =
            Properties().apply {
                versionsFile.inputStream().use(::load)
            }
        val unitTestDslVersion =
            versions.getProperty("unitTestDslLibraryVersion")
                ?: error("Missing unitTestDslLibraryVersion in ${versionsFile.path}")

        settings.dependencyResolutionManagement.versionCatalogs.create("testLibs") {
            library(
                "com.marmatsan.repo.unit.test.dsl",
                "com.marmatsan.repo",
                "unit-test-dsl",
            ).version(unitTestDslVersion)
        }
    }

    private fun findVersionsFile(
        rootDirectory: File,
    ): File =
        listOf(
            rootDirectory.resolve("repo/water-my-plants-project-config/versions.properties"),
            rootDirectory.resolve("versions.properties"),
        ).firstOrNull(File::isFile)
            ?: error("Water My Plants versions.properties not found from ${rootDirectory.path}")
}
