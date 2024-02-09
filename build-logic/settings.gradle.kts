rootProject.name = "build-logic"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs"){
            val applicationVersion = "8.2.2"
            val kotlinVersion = "1.9.22"

            val versions = mapOf(
                "applicationVersion" to applicationVersion,
                "kotlinVersion" to kotlinVersion
            )

            versions.entries.forEach {
                version(it.key, it.value)
            }

            library("build.gradle", "com.android.tools.build", "gradle").versionRef("applicationVersion")
            library("kotlin.gradle.plugin", "org.jetbrains.kotlin", "kotlin-gradle-plugin").versionRef("kotlinVersion")
            library("kotlin.android.extensions", "org.jetbrains.kotlin", "kotlin-android-extensions").versionRef("kotlinVersion")
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":android",
    ":compose",
    ":dependencies"
)