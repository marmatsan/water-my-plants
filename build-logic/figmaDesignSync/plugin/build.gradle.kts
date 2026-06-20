plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(plugins.plugins.com.google.devtools.ksp)
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
    implementation(projects.figmaDesignSync.domain)
    implementation(projects.figmaDesignSync.data)

    ksp(libs.me.tatarka.inject.kotlin.inject.compiler.ksp)

    implementation(libs.me.tatarka.inject.kotlin.inject.runtime)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    // Kotest
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

gradlePlugin {
    val pluginName = "com.marmatsan.figmaDesignSync"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "${pluginName}.plugin.gradle.figmaDesignSyncGradleConventionPlugin"
    }
}
