package com.marmatsan.dev.watermyplants.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.marmatsan.catalog_data.di.CatalogDataComponent
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.core_domain.ProtoPreferences
import com.marmatsan.dev.catalog_domain.di.CatalogDomainComponent
import com.marmatsan.dev.core_data.preferences.PreferencesDataSerializer
import com.marmatsan.dev.core_data.preferences.PreferencesImpl
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
) : CatalogDomainComponent,
    CatalogDataComponent {

    // Proto datastore
    companion object {
        private const val DATA_STORE_FILE_NAME = "preferences_data.pb"
    }


    @Singleton
    @Provides
    fun provideProtoDataStore(
        appContext: Context = context
    ): DataStore<ProtoPreferences> = DataStoreFactory.create(
        serializer = PreferencesDataSerializer,
        produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) }
    )

    @Singleton
    @Provides
    fun providePreferences(dataStore: DataStore<ProtoPreferences>): Preferences =
        PreferencesImpl(dataStore)

    @Provides
    @Singleton
    fun provideRealmDatabase(): Realm = Realm.open(
        configuration = RealmConfiguration
            .Builder(
                schema = setOf(
                    RealmPlant::class
                )
            )
            .schemaVersion(4)
            .deleteRealmIfMigrationNeeded()
            .build()
    )
}

interface ApplicationComponentProvider {
    val component: ApplicationComponent
}

val Context.applicationComponent get() = (applicationContext as ApplicationComponentProvider).component