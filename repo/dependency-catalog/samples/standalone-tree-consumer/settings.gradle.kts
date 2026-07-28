pluginManagement {
    repositories {
        maven {
            url = uri(providers.gradleProperty("dependencyCatalogPublicationRepository").get())
        }
        gradlePluginPortal()
    }

    plugins {
        id("com.marmatsan.dependencyCatalog.tree") version
            providers.gradleProperty("dependencyCatalogVersion").get()
    }
}

plugins {
    id("com.marmatsan.dependencyCatalog.tree")
}

dependencyCatalogTree {
    libraries {
        root("com") {
            library("example.tools") {
                artifact(
                    artifact = "tools-core",
                    version = version("exampleLibraryVersion"),
                )
            }
        }
    }

    plugins {
        root("com") {
            plugin(
                id = "example.quality",
                version = version("examplePluginVersion"),
            )
        }
    }
}

rootProject.name = "dependency-catalog-standalone-tree-consumer"
