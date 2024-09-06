plugins {
    alias(plugins.plugins.com.android.library)
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.serialization)
    id("com.marmatsan.android")
    id("com.marmatsan.compose")
}

dependencies {
    /* Modules */
    implementation(projects.core.coreDomain)

    /* Libraries */
    implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
}