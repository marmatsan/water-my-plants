package com.marmatsan.dev.core_data.preferences

import androidx.datastore.core.DataStore
import com.marmatsan.core_domain.UserPreferences
import com.marmatsan.dev.core_domain.preferences.Preferences
import kotlinx.coroutines.flow.first


class DefaultPreferences(
    private val dataStore: DataStore<UserPreferences>
) : Preferences {
    override suspend fun saveIsMediaAccessPermanentlyDeclined(declined: Boolean) {
        dataStore.updateData {
            it.toBuilder().setIsMediaAccessPermanentlyDeclined(declined).build()
        }
    }

    override suspend fun readIsMediaAccessPermanentlyDeclined(): Boolean {
        return dataStore.data.first().isMediaAccessPermanentlyDeclined
    }

}