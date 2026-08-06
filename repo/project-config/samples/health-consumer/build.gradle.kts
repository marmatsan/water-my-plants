import com.marmatsan.figmaDocumentationSync.plugin.gradle.figmaDocumentationSyncExtension
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

plugins {
    id("com.marmatsan.projectConfig.figma")
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
check(pluginManager.hasPlugin("com.marmatsan.projectConfig.figma"))
val figmaCatalogTreesJson =
    extensions
        .getByType(figmaDocumentationSyncExtension::class.java)
        .dependencyCatalogTreesJson

abstract class VerifyProjectConfigTask : DefaultTask() {
    @get:Input
    abstract val dependencyCatalogTreesJson: Property<String>

    @TaskAction
    fun verify() {
        val catalogJson = dependencyCatalogTreesJson.get()
        check("ktor-client-core" in catalogJson)
        check("org" in catalogJson)
    }
}

tasks.register<VerifyProjectConfigTask>("verifyProjectConfig") {
    group = "verification"
    description = "Verifies source-independent reusable composition and its optional Figma adapter."
    dependencyCatalogTreesJson.set(figmaCatalogTreesJson)
}
