// Top-level build file.
plugins {
    alias(plugins.plugins.com.android.application) apply false
    alias(plugins.plugins.com.android.library) apply false
    alias(plugins.plugins.org.jetbrains.kotlin.android) apply false
    alias(plugins.plugins.com.google.devtools.ksp) apply false
    alias(plugins.plugins.com.google.dagger.hilt.android) apply false
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.serialization) apply false
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.parcelize) apply false
    alias(plugins.plugins.com.google.protobuf) apply false
}