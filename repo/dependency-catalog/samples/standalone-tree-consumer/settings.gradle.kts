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
        library(
            group = "com.example.tools",
            artifact = "tools-core",
            version = version("exampleLibraryVersion"),
        )
    }

    plugins {
        plugin(
            id = "com.example.quality",
            version = version("examplePluginVersion"),
        )
    }
}

rootProject.name = "dependency-catalog-standalone-tree-consumer"
