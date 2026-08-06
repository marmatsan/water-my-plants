@file:Suppress("AvoidDuplicateDependencies")

import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(plugins.plugins.org.jetbrains.dokka)
}

dependencies {
    implementation(libs.com.marmatsan.repo.catalog.api)
    implementation(libs.com.marmatsan.repo.catalog.gradle.plugin)
    implementation(projects.catalog)
    implementation(libs.com.marmatsan.figma.documentation.sync.domain)
    implementation(libs.com.marmatsan.figma.documentation.sync.data)
    implementation(libs.com.marmatsan.figma.documentation.sync.plugin)
    implementation(libs.com.marmatsan.figma.documentation.sync.teamcity.adapter)
    implementation(libs.com.marmatsan.figma.documentation.sync.teamcity.operations)

    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.bundles.kotestBundle)
    testImplementation(libs.org.jetbrains.kotlinx.serialization.json)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty(
        "figmaDocumentationSyncWriterRuntimeContract",
        rootProject
            .file(
                "../figma-documentation-sync/tools/fixtures/contracts/writer-runtime-contract.json"
            ).absolutePath
    )
}

gradlePlugin {
    plugins.register("com.marmatsan.waterMyPlantsSettings") {
        id = "com.marmatsan.waterMyPlantsSettings"
        implementationClass = "com.marmatsan.waterMyPlants.projectConfig.gradle.WaterMyPlantsSettingsPlugin"
    }
    plugins.register("com.marmatsan.waterMyPlantsProjectConfig") {
        id = "com.marmatsan.waterMyPlantsProjectConfig"
        implementationClass = "com.marmatsan.waterMyPlants.projectConfig.gradle.WaterMyPlantsProjectConfigPlugin"
    }
}

dokka {
    moduleName.set("water-my-plants-project-config-plugin")

    dokkaPublications.html {
        failOnWarning.set(true)
        includes.from(
            "docs/dokka/README.md"
        )
    }

    dokkaSourceSets.main {
        documentedVisibilities.set(
            setOf(
                VisibilityModifier.Public,
                VisibilityModifier.Internal
            )
        )
        reportUndocumented.set(true)

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/water-my-plants-project-config/plugin/src/main/kotlin"
                )
            )
            remoteLineSuffix.set("#L")
        }
    }
}

tasks.named("check") {
    dependsOn("dokkaGenerate")
}
