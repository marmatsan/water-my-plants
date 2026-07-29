@file:Suppress("AvoidDuplicateDependencies")

plugins {
    alias(plugins.plugins.com.android.library)
    alias(plugins.plugins.com.marmatsan.android)
    alias(plugins.plugins.com.marmatsan.compose)
}

android {
    namespace = "com.marmatsan.onboarding.ui"
}

dependencies {
    implementation(projects.core.ui)
}
