plugins {
    alias(plugins.plugins.com.android.library)
    id("com.marmatsan.android")
}

dependencies {
    /* Modules */
    implementation(projects.catalog.catalogDomain)
}