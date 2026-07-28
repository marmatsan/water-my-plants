@file:Suppress("AvoidDuplicateDependencies")

import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI

plugins {
    alias(plugins.plugins.org.jetbrains.kotlin.jvm)
    alias(plugins.plugins.org.jetbrains.dokka)
}

group = "com.marmatsan.repo"

dependencies {
    implementation(libs.com.marmatsan.repo.catalog.api)
    implementation(libs.com.marmatsan.repo.catalog.core)
}

dokka {
    moduleName.set("water-my-plants-project-config-catalog")

    dokkaPublications.html {
        failOnWarning.set(true)
        includes.from(
            "docs/dokka/README.md",
        )
    }

    dokkaSourceSets.main {
        documentedVisibilities.set(
            setOf(
                VisibilityModifier.Public,
                VisibilityModifier.Internal,
            ),
        )
        reportUndocumented.set(true)

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/water-my-plants-project-config/catalog/src/main/kotlin",
                ),
            )
            remoteLineSuffix.set("#L")
        }
    }
}

tasks.named("check") {
    dependsOn("dokkaGenerate")
}
