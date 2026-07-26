plugins {
    base
}

tasks.named("check") {
    dependsOn(
        ":catalog:check",
        ":plugin:check",
    )
}
