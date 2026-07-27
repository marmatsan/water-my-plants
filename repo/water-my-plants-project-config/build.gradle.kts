plugins {
    base
}

tasks.named("check") {
    dependsOn(
        ":catalog:check",
        ":plugin:check",
    )
}

tasks.register("dokkaGenerate") {
    group = "documentation"
    description = "Generates the Water My Plants catalog and composition-plugin API references."
    dependsOn(
        ":catalog:dokkaGenerate",
        ":plugin:dokkaGenerate",
    )
}
