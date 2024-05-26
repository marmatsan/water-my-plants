package com.marmatsan.dev.core_domain.preferences

import com.marmatsan.core_domain.PreferencesData
import kotlinx.coroutines.flow.Flow

interface Preferences {
    // Save data
    suspend fun saveIsPlantNameValid(isPlantNameValid: Boolean)
    suspend fun saveIsWateringDaysValid(isWateringDaysValid: Boolean)
    suspend fun saveIsWateringTimeValid(isWateringTimeValid: Boolean)

    // Read data
    fun preferencesDataFlow(): Flow<PreferencesData>
}