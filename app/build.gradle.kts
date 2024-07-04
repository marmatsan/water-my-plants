plugins {
    alias(plugins.plugins.com.android.application)
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(plugins.plugins.de.mannodermaus.android.junit5)
    id("com.marmatsan.android")
    id("com.marmatsan.compose")
}

android {
    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    /* Modules */
    // Core
    implementation(projects.core.coreData)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUi)
    // Catalog
    implementation(projects.catalog.catalogData)
    implementation(projects.catalog.catalogDomain)
    implementation(projects.catalog.catalogUi)
    // Notifications
    implementation(projects.notifications.notificationsData)
    implementation(projects.notifications.notificationsDomain)

    /* Libraries */
    implementation(libs.androidx.core.core.splashscreen)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
    implementation(libs.bundles.androidx.datastore.datastore)
    implementation(libs.com.google.protobuf)
    implementation(libs.androidx.lifecycle)
}