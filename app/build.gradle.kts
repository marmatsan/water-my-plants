plugins {
    alias(plugins.plugins.com.android.application)
    id("com.marmatsan.android")
    id("com.marmatsan.bddTest")
    id("com.marmatsan.compose")
    id("com.marmatsan.unitTest")
}

dependencies {
    /* Modules */
    // Core
    implementation(projects.core.coreUi)
}
