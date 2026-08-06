package com.marmatsan.figmaDocumentationSync.plugin.gradle.platform

/** Host operating-system facts needed by portable command registration. */
object HostOperatingSystem {
    /** Whether commands must use Windows executable and shell conventions. */
    val isWindows: Boolean =
        System.getProperty("os.name").startsWith(
            "Windows",
            ignoreCase = true
        )
}
