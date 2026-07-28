@file:Suppress("AvoidDuplicateDependencies")

plugins {
    alias(plugins.plugins.com.android.application)
    alias(plugins.plugins.com.marmatsan.android)
    alias(plugins.plugins.com.marmatsan.bddTest)
    alias(plugins.plugins.com.marmatsan.compose)
    alias(plugins.plugins.com.marmatsan.unitTest)
}

dependencies {
    // Modules
    // Core
    implementation(projects.core.ui)
}
