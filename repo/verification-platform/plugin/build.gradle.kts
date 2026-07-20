import java.net.URI

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    `java-gradle-plugin`
}

dependencies {
    implementation(projects.domain)
    implementation(projects.data) {
        exclude(
            group = "com.pinterest.ktlint",
        )
    }
    implementation(gradleApi())
}

gradlePlugin {
    plugins.register("com.marmatsan.verificationPlatform") {
        id = "com.marmatsan.verificationPlatform"
        implementationClass = "com.marmatsan.verificationPlatform.plugin.VerificationPlatformPlugin"
        displayName = "Water My Plants Verification Platform"
        description = "Generates the typed repository verification plan consumed by CI adapters."
    }
}

tasks.named("check") {
    dependsOn("dokkaGenerate")
}

dokka {
    moduleName.set("verification-platform-plugin")

    dokkaPublications.html {
        failOnWarning.set(true)
        includes.from(
            "docs/dokka/README.md",
        )
    }

    dokkaSourceSets.main {
        reportUndocumented.set(true)

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/verification-platform/plugin/src/main/kotlin",
                ),
            )
            remoteLineSuffix.set("#L")
        }
    }
}
