@file:Suppress("AvoidDuplicateDependencies")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    // Modules
    implementation(projects.dependencies)

    // Kotest
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.platform.launcher)
    // MockK
    testImplementation(libs.io.mockk)
}

gradlePlugin {
    val pluginName = "com.marmatsan.bddTest"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "$pluginName.plugin.BddTestGradleConventionPlugin"
    }
}
