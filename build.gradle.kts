// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(plugins.plugins.com.android.application) apply false
    alias(plugins.plugins.com.android.library) apply false
    alias(plugins.plugins.com.figma.code.connect) apply false
    alias(plugins.plugins.com.google.devtools.ksp) apply false
    alias(plugins.plugins.com.google.protobuf) apply false
    alias(plugins.plugins.de.mannodermaus.android.junit5) apply false
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.compose) apply false
    id("com.marmatsan.android") apply false
    id("com.marmatsan.compose") apply false
    id("com.marmatsan.protobuf") apply false
    id("com.marmatsan.unitTest") apply false
}
