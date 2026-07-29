@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(plugins.plugins.org.jetbrains.kotlin.jvm)
    alias(plugins.plugins.org.jetbrains.dokka)
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(projects.catalogApi)

    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.io.mockk)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

gradlePlugin {
    val pluginName = "com.marmatsan.dependencyCatalog"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "com.marmatsan.dependencies.gradle.DependencyCatalogSettingsPlugin"
        displayName = "Dependency Catalog"
        description = "Registers repository-owned dependency trees as Gradle version catalogs."
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (name == "pluginMaven") {
            artifactId = "catalog-gradle-plugin"
        }

        pom {
            name.set("Dependency Catalog Gradle Plugin")
            description.set("Gradle settings adapter for portable dependency catalog trees.")
        }
    }
}
