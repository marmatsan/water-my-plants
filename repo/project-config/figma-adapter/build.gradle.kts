@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    alias(plugins.plugins.org.jetbrains.dokka)
}

dependencies {
    implementation(projects.plugin)
    implementation(libs.com.marmatsan.repo.catalog.api)
    implementation(libs.com.marmatsan.figma.documentation.sync.domain)
    implementation(libs.com.marmatsan.figma.documentation.sync.data)
    implementation(libs.com.marmatsan.figma.documentation.sync.plugin)

    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.bundles.kotestBundle)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

gradlePlugin {
    plugins.register("projectConfigFigma") {
        id = "com.marmatsan.projectConfig.figma"
        implementationClass = "com.marmatsan.projectConfig.figma.ProjectConfigFigmaGradlePlugin"
        displayName = "Reusable Project Config Figma Adapter"
        description = "Projects a consumer-owned dependency catalog into Figma documentation sync."
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (name == "pluginMaven") {
            artifactId = "project-config-figma-gradle-plugin"
        }
        pom {
            name.set("Reusable Project Config Figma Adapter")
            description.set("Optional adapter from reusable project configuration to Figma documentation sync.")
        }
    }
}
