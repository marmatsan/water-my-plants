plugins {
    alias(plugins.plugins.com.android.library)
    id("com.marmatsan.android")
    id("com.marmatsan.compose")
}

android {
    namespace = "com.marmatsan.onboarding.ui"
}

dependencies {
    implementation(projects.core.ui)
}
