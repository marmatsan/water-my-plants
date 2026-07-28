import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.dependencies.catalog.api.LibraryCatalogEntry
import com.marmatsan.dependencies.catalog.api.LibraryCatalogNode
import com.marmatsan.dependencies.catalog.api.PluginCatalogNode
import com.marmatsan.dependencies.catalog.api.ResolvedDependencyCatalogProvider
import java.io.File

pluginManagement {
    repositories {
        maven {
            url = uri(providers.gradleProperty("dependencyCatalogPublicationRepository").get())
        }
        gradlePluginPortal()
    }

    plugins {
        id("com.marmatsan.dependencyCatalog") version
            providers.gradleProperty("dependencyCatalogVersion").get()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri(providers.gradleProperty("dependencyCatalogPublicationRepository").get())
        }
        mavenCentral()
    }
}

plugins {
    id("com.marmatsan.dependencyCatalog")
}

dependencyCatalog {
    from(
        provider =
            object : ResolvedDependencyCatalogProvider {
                override fun resolved(
                    rootDir: File,
                ): DependencyCatalog =
                    DependencyCatalog(
                        libraries =
                            listOf(
                                LibraryCatalogNode(
                                    group = "org",
                                    children =
                                        listOf(
                                            LibraryCatalogNode(
                                                group = "jetbrains",
                                                children =
                                                    listOf(
                                                        LibraryCatalogNode(
                                                            group = "kotlin",
                                                            entries =
                                                                listOf(
                                                                    LibraryCatalogEntry.Artifact(
                                                                        name = "kotlin-stdlib",
                                                                        version = "2.4.0",
                                                                    ),
                                                                ),
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                            ),
                        plugins =
                            listOf(
                                PluginCatalogNode(
                                    id = "org",
                                    children =
                                        listOf(
                                            PluginCatalogNode(
                                                id = "jetbrains",
                                                children =
                                                    listOf(
                                                        PluginCatalogNode(
                                                            id = "kotlin",
                                                            children =
                                                                listOf(
                                                                    PluginCatalogNode(
                                                                        id = "jvm",
                                                                        version = "2.4.0",
                                                                    ),
                                                                ),
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                            ),
                    )
            },
    )
}

rootProject.name = "dependency-catalog-standalone-consumer"
