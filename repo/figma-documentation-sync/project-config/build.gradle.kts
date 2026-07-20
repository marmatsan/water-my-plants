@file:Suppress("AvoidDuplicateDependencies")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(projects.data)
    implementation(projects.domain)
    implementation(projects.plugin)
    implementation(projects.teamcityAdapter)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)
    implementation("com.marmatsan.repo:catalog-core")
    implementation("com.marmatsan.repo:water-my-plants-catalog")

    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty(
        "figmaDocumentationSyncWriterRuntimeContract",
        rootProject.file("tools/fixtures/contracts/writer-runtime-contract.json").absolutePath,
    )
}

gradlePlugin {
    val pluginName = "com.marmatsan.waterMyPlantsFigmaDocumentationSync"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass =
            "com.marmatsan.figmaDocumentationSync.projectConfig.WaterMyPlantsFigmaDocumentationSyncGradlePlugin"
    }
}
