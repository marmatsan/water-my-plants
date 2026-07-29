@file:Suppress("AvoidDuplicateDependencies")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(libs.org.jetbrains.dokka.gradle.plugin)
    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)

    testImplementation(libs.bundles.kotest)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

gradlePlugin {
    val pluginName = "com.marmatsan.dokkaDocumentation"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "$pluginName.plugin.DokkaDocumentationGradleConventionPlugin"
    }
}
