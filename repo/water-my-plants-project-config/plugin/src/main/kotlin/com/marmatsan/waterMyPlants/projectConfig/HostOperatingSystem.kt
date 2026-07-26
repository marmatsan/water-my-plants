package com.marmatsan.waterMyPlants.projectConfig

/** Host operating-system facts needed by product-owned command registration. */
internal object HostOperatingSystem {
    val isWindows: Boolean =
        System.getProperty("os.name").startsWith(
            "Windows",
            ignoreCase = true,
        )
}
