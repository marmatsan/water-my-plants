import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.dependencies.catalog.api.DependencyCatalogProvider
import com.marmatsan.dependencies.catalog.api.LibraryCatalogEntry
import com.marmatsan.dependencies.catalog.api.LibraryCatalogNode
import com.marmatsan.dependencies.catalog.api.PluginCatalogNode
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
            object : DependencyCatalogProvider {
                override fun resolved(
                    rootDir: File,
                ): DependencyCatalog =
                    DependencyCatalog(
                        libraries =
                            listOf(
                                LibraryCatalogNode(
                                    group = "org.jetbrains.kotlin",
                                    entries =
                                        listOf(
                                            LibraryCatalogEntry.Artifact(
                                                name = "kotlin-stdlib",
                                                version = "2.4.0",
                                            ),
                                        ),
                                ),
                            ),
                        plugins =
                            listOf(
                                PluginCatalogNode(
                                    id = "org.jetbrains.kotlin.jvm",
                                    version = "2.4.0",
                                ),
                            ),
                    )

                override fun withVersionAliases(): DependencyCatalog =
                    resolved(
                        rootDir = File("."),
                    )
            },
    )
}

rootProject.name = "dependency-catalog-standalone-consumer"
