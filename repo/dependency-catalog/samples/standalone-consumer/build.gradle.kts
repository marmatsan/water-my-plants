@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.artifacts.VersionCatalogsExtension

tasks.register("verifyCatalogs") {
    group = "verification"
    description = "Verifies that the staged plugin registers consumer-owned catalogs without source includes."

    doLast {
        val catalogs = project.extensions.getByType<VersionCatalogsExtension>()
        check(catalogs.named("libs").findLibrary("org.jetbrains.kotlin.stdlib").isPresent)
        check(catalogs.named("plugins").findPlugin("org.jetbrains.kotlin.jvm").isPresent)
    }
}
