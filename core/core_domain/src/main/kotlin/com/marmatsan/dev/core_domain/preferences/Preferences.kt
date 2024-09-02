package com.marmatsan.dev.core_domain.preferences

interface Preferences {
    // Save data
    suspend fun saveIsMediaAccessPermanentlyDeclined(permanentlyDeclined: Boolean)

    // Load data
    suspend fun readIsMediaAccessPermanentlyDeclined(): Boolean
}