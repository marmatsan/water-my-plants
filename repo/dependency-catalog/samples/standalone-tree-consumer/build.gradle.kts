@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    base
}

tasks.register("verifyCatalogs") {
    group = "verification"
    description = "Verifies tree-backed catalogs resolved only from staged publications."

    doLast {
        val catalogs = project.extensions.getByType<VersionCatalogsExtension>()
        check(catalogs.named("libs").findLibrary("com.example.tools.core").isPresent)
        check(catalogs.named("plugins").findPlugin("com.example.quality").isPresent)
    }
}
