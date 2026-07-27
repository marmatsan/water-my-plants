package com.marmatsan.waterMyPlants.projectConfig.platform

/** Host operating-system facts needed by product-owned command registration. */
internal object HostOperatingSystem {
    /** Whether product commands must use Windows executable and shell conventions. */
    val isWindows: Boolean =
        System.getProperty("os.name").startsWith(
            "Windows",
            ignoreCase = true,
        )
}
