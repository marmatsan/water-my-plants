@file:Suppress("AvoidDuplicateDependencies")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

val portableVersion = providers.gradleProperty("figmaDocumentationSyncVersion").getOrElse("0.1.0-SNAPSHOT")

dependencies {
    implementation("com.marmatsan.repo:catalog-api:$portableVersion")
    implementation("com.marmatsan.repo:catalog-gradle-plugin:$portableVersion")
    implementation(projects.catalog)
    implementation("com.marmatsan.figma-documentation-sync:domain:$portableVersion")
    implementation("com.marmatsan.figma-documentation-sync:data:$portableVersion")
    implementation("com.marmatsan.figma-documentation-sync:plugin:$portableVersion")
    implementation("com.marmatsan.figma-documentation-sync:teamcity-adapter:$portableVersion")
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty(
        "figmaDocumentationSyncWriterRuntimeContract",
        rootProject
            .file(
                "../figma-documentation-sync/tools/fixtures/contracts/writer-runtime-contract.json",
            ).absolutePath,
    )
}

gradlePlugin {
    plugins.register("com.marmatsan.waterMyPlantsSettings") {
        id = "com.marmatsan.waterMyPlantsSettings"
        implementationClass = "com.marmatsan.waterMyPlants.projectConfig.WaterMyPlantsSettingsPlugin"
    }
    plugins.register("com.marmatsan.waterMyPlantsProjectConfig") {
        id = "com.marmatsan.waterMyPlantsProjectConfig"
        implementationClass = "com.marmatsan.waterMyPlants.projectConfig.WaterMyPlantsProjectConfigPlugin"
    }
}
