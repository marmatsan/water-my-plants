package com.marmatsan.dev.core_data.preferences

import androidx.datastore.core.DataStore
import com.marmatsan.core_domain.PreferencesData
import com.marmatsan.dev.core_domain.preferences.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import me.tatarka.inject.annotations.Inject
import java.io.IOException

@Inject
class DefaultPreferences(
    private val dataStore: DataStore<PreferencesData>
) : Preferences {
    override suspend fun saveIsPlantNameValid(isPlantNameValid: Boolean) {
        dataStore.updateData { preferencesData ->
            preferencesData.toBuilder().setIsPlantNameValid(isPlantNameValid).build()
        }
    }

    override suspend fun saveIsWateringDaysValid(isWateringDaysValid: Boolean) {
        dataStore.updateData { preferencesData ->
            preferencesData.toBuilder().setIsWateringDaysValid(isWateringDaysValid).build()
        }
    }

    override suspend fun saveIsWateringTimeValid(isWateringTimeValid: Boolean) {
        dataStore.updateData { preferencesData ->
            preferencesData.toBuilder().setIsWateringTimeValid(isWateringTimeValid).build()
        }
    }

    override fun preferencesDataFlow(): Flow<PreferencesData> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(PreferencesData.getDefaultInstance())
        } else {
            throw exception
        }
    }
}