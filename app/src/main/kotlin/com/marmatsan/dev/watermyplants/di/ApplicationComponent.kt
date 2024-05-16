package com.marmatsan.dev.watermyplants.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.marmatsan.catalog_data.di.CatalogDataComponent
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.core_domain.PreferencesData
import com.marmatsan.dev.catalog_domain.di.CatalogDomainComponent
import com.marmatsan.dev.core_data.preferences.DefaultPreferences
import com.marmatsan.dev.core_data.preferences.PreferencesDataSerializer
import com.marmatsan.dev.core_domain.di.Singleton
import com.marmatsan.dev.core_domain.preferences.Preferences
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@Singleton
abstract class ApplicationComponent(
    @get:Provides val context: Context,
) : CatalogDomainComponent, CatalogDataComponent {
    @Provides
    @Singleton
    fun provideRealmDatabase(): Realm {
        return Realm.open(
            configuration = RealmConfiguration.create(
                schema = setOf(
                    RealmPlant::class
                )
            )
        )
    }

    private val dataStoreFileName: String
        get() = "preferences_data.pb"

    @Singleton
    @Provides
    fun provideProtoDataStore(): DataStore<PreferencesData> {
        return DataStoreFactory.create(
            serializer = PreferencesDataSerializer,
            produceFile = { context.dataStoreFile(dataStoreFileName) }
        )
    }

    @Singleton
    @Provides
    fun providePreferences(dataStore: DataStore<PreferencesData>): Preferences {
        return DefaultPreferences(dataStore)
    }
}

interface ApplicationComponentProvider {
    val component: ApplicationComponent
}

val Context.applicationComponent get() = (applicationContext as ApplicationComponentProvider).component
