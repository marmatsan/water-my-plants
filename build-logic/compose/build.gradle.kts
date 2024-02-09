plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    /* Modules */
    implementation(projects.dependencies)

    /* Libraries */
    compileOnly(libs.build.gradle)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.android.extensions)
}

gradlePlugin {
    plugins.register("com.marmatsan.compose") {
        id = "com.marmatsan.compose"
        implementationClass = "com.marmatsan.compose.ComposePlugin"
    }
}