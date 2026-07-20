plugins {
    base
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.kotlin.plugin.serialization") apply false
    id("org.jetbrains.dokka") apply false
}

tasks.named("check") {
    dependsOn(
        ":domain:check",
        ":data:check",
        ":plugin:check",
    )
}

tasks.register("dokkaGenerate") {
    group = "documentation"
    description = "Generates the CI domain, data, and plugin API documentation."
    dependsOn(
        ":domain:dokkaGenerate",
        ":data:dokkaGenerate",
        ":plugin:dokkaGenerate",
    )
}
