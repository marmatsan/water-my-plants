plugins {
    id("org.jetbrains.kotlin.jvm")
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
