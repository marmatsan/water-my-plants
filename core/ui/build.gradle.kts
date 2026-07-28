@file:Suppress("AvoidDuplicateDependencies")

plugins {
    alias(plugins.plugins.com.android.library)
    id("com.marmatsan.android")
    id("com.marmatsan.compose")
}

android {
    namespace = "com.marmatsan.core.ui"
}
