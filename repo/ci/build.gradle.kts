plugins {
    base
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.kotlin.plugin.serialization") apply false
}

tasks.named("check") {
    dependsOn(":domain:check", ":data:check", ":plugin:check")
}
