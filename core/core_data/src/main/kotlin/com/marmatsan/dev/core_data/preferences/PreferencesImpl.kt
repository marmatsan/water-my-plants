package com.marmatsan.dev.core_data.preferences

import androidx.datastore.core.DataStore
import com.marmatsan.core_domain.ProtoPreferences
import com.marmatsan.dev.core_domain.preferences.Preferences
import kotlinx.coroutines.flow.first
import me.tatarka.inject.annotations.Inject

@Inject
class PreferencesImpl(
    private val dataStore: DataStore<ProtoPreferences>
) : Preferences {
    override suspend fun saveIsMediaAccessPermanentlyDeclined(permanentlyDeclined: Boolean) {
        dataStore.updateData {
            it.toBuilder().setIsMediaAccessPermanentlyDeclined(permanentlyDeclined).build()
        }
    }

    override suspend fun readIsMediaAccessPermanentlyDeclined(): Boolean =
        dataStore.data.first().isMediaAccessPermanentlyDeclined
}