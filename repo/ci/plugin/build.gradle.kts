import java.net.URI

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    `java-gradle-plugin`
}

dependencies {
    implementation(projects.domain)
    implementation(projects.data)
    implementation(gradleApi())
}

gradlePlugin {
    plugins.register("com.marmatsan.ci") {
        id = "com.marmatsan.ci"
        implementationClass = "com.marmatsan.ci.plugin.CiGradlePlugin"
        displayName = "Water My Plants CI Planner"
        description = "Generates the typed repository verification plan consumed by CI adapters."
    }
}

tasks.named("check") {
    dependsOn("dokkaGenerate")
}

dokka {
    moduleName.set("ci-plugin")

    dokkaPublications.html {
        failOnWarning.set(true)
        includes.from("docs/dokka/README.md")
    }

    dokkaSourceSets.main {
        reportUndocumented.set(true)

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/ci/plugin/src/main/kotlin"
                )
            )
            remoteLineSuffix.set("#L")
        }
    }
}
