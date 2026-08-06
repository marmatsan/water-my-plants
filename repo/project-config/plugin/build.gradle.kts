@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    alias(plugins.plugins.org.jetbrains.dokka)
}

dependencies {
    implementation(libs.com.marmatsan.repo.catalog.core)
    implementation(libs.com.marmatsan.repo.catalog.gradle.plugin)
}

gradlePlugin {
    plugins.register("projectConfigSettings") {
        id = "com.marmatsan.projectConfig.settings"
        implementationClass = "com.marmatsan.projectConfig.settings.ProjectConfigSettingsPlugin"
        displayName = "Reusable Project Config Settings"
        description = "Builds consumer-owned dependency catalogs from one reusable tree definition."
    }
    plugins.register("projectConfig") {
        id = "com.marmatsan.projectConfig"
        implementationClass = "com.marmatsan.projectConfig.project.ProjectConfigGradlePlugin"
        displayName = "Reusable Project Config"
        description = "Composes optional repository capabilities without embedding product identity."
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (name == "pluginMaven") {
            artifactId = "project-config-gradle-plugin"
        }
        pom {
            name.set("Reusable Project Config Gradle Plugin")
            description.set("Consumer-owned catalog and repository capability composition.")
        }
    }
}
