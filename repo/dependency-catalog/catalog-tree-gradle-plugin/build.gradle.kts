@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(plugins.plugins.org.jetbrains.kotlin.jvm)
    alias(plugins.plugins.org.jetbrains.dokka)
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    api(projects.catalogCore)
    implementation(projects.catalogApi)
    implementation(projects.catalogGradlePlugin)

    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.bundles.kotestBundle)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

gradlePlugin {
    val pluginName = "com.marmatsan.dependencyCatalog.tree"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass =
            "com.marmatsan.dependencies.gradle.tree.TreeDependencyCatalogSettingsPlugin"
        displayName = "Tree Dependency Catalog"
        description = "Builds Gradle version catalogs from compact dependency trees during settings evaluation."
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (name == "pluginMaven") {
            artifactId = "catalog-tree-gradle-plugin"
        }

        pom {
            name.set("Tree Dependency Catalog Gradle Plugin")
            description.set("Tree DSL settings adapter for portable dependency catalogs.")
        }
    }
}
