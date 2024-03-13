plugins {
    alias(plugins.plugins.com.android.application)
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
    implementation(projects.core.coreUi)
    implementation(projects.catalog.catalogUi)
    implementation(projects.onboarding.onboardingUi)

    /* Libraries */
    implementation(libs.androidx.core.core.splashscreen)
    implementation(libs.bundles.androidx.datastore.datastore)
    implementation(libs.com.google.protobuf)
}