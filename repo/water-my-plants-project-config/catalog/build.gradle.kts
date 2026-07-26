plugins {
    id("org.jetbrains.kotlin.jvm")
}

group = "com.marmatsan.repo"

val portableVersion = providers.gradleProperty("figmaDocumentationSyncVersion").getOrElse("0.1.0-SNAPSHOT")

dependencies {
    implementation("com.marmatsan.repo:catalog-api:$portableVersion")
    implementation("com.marmatsan.repo:catalog-core:$portableVersion")
}
