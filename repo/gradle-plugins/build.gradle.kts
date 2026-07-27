@file:Suppress("AvoidDuplicateDependencies")

plugins {
    base
    `kotlin-dsl` apply false
}

tasks.named("check") {
    dependsOn(
        ":android:check",
        ":bdd-test:check",
        ":compose:check",
        ":dependencies:check",
        ":dokka-documentation:check",
        ":protobuf:check",
        ":unit-test:check",
    )
}
