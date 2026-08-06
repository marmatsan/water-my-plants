import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("com.marmatsan.projectConfig")
}

val catalogs = extensions.getByType(VersionCatalogsExtension::class.java)
val libraries = catalogs.named("libs")
val projectPlugins = catalogs.named("plugins")
val ktorClientCore =
    libraries
        .findLibrary("io.ktor.client.core")
        .orElseThrow()
        .get()

check(ktorClientCore.module.toString() == "io.ktor:ktor-client-core")
check(ktorClientCore.versionConstraint.requiredVersion == "3.3.0")
check(
    projectPlugins
        .findPlugin("org.jetbrains.kotlin.jvm")
        .orElseThrow()
        .get()
        .pluginId == "org.jetbrains.kotlin.jvm"
)
check(pluginManager.hasPlugin("com.marmatsan.projectConfig"))

tasks.register("verifyProjectConfig") {
    group = "verification"
    description = "Verifies source-independent reusable composition for the Health fixture."
}
