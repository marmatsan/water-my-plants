plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(plugins.plugins.com.google.devtools.ksp)
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(projects.figmaCatalogChecks.domain)
    implementation(projects.figmaCatalogChecks.data)

    ksp(libs.me.tatarka.inject.kotlin.inject.compiler.ksp)

    implementation(libs.me.tatarka.inject.kotlin.inject.runtime)
}

gradlePlugin {
    val pluginName = "com.marmatsan.figmaCatalogChecks"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "${pluginName}.plugin.gradle.FigmaCatalogChecksGradleConventionPlugin"
    }
}
