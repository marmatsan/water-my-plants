plugins {
    alias(plugins.plugins.com.android.library)
    id("com.marmatsan.android")
}

dependencies {
    // Modules
    /* Core */
    implementation(projects.core.coreDomain)
}