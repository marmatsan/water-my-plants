plugins {
    alias(plugins.plugins.com.android.library)
    id("com.marmatsan.android")
    id("com.marmatsan.compose")
}

dependencies {
    /* Modules */
    // Core
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUi)
    // Catalog
    implementation(projects.catalog.catalogDomain)
}