@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(plugins.plugins.org.jetbrains.kotlin.jvm)
    alias(plugins.plugins.org.jetbrains.dokka)
    `java-gradle-plugin`
    `maven-publish`
}

java {
    withSourcesJar()
}

dependencies {
    api(projects.catalogCore)
    implementation(projects.catalogApi)
    implementation(projects.catalogGradlePlugin)
    implementation(gradleApi())

    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(gradleTestKit())
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
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
            url.set("https://github.com/marmatsan/water-my-plants/tree/main/repo/dependency-catalog")
            scm {
                connection.set("scm:git:https://github.com/marmatsan/water-my-plants.git")
                url.set("https://github.com/marmatsan/water-my-plants")
            }
        }
    }

    repositories {
        maven {
            name = "staging"
            url =
                uri(
                    providers.gradleProperty("dependencyCatalogPublicationRepository").orNull
                        ?: rootProject.layout.buildDirectory
                            .dir("publication-repository")
                            .get()
                            .asFile,
                )
        }
    }
}
