package com.marmatsan.dev.watermyplants.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.marmatsan.catalog_data.di.CatalogDataComponent
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.core_domain.UserPreferences
import com.marmatsan.dev.catalog_domain.di.CatalogDomainComponent
import com.marmatsan.dev.core_data.preferences.DefaultPreferences
import com.marmatsan.dev.core_data.preferences.PreferencesDataSerializer
import com.marmatsan.dev.core_domain.di.Singleton
import com.marmatsan.dev.core_domain.preferences.Preferences
import com.marmatsan.dev.notifications_data.di.NotificationsDataComponent
import com.marmatsan.dev.notifications_domain.di.NotificationsDomainComponent
import com.marmatsan.dev.notifications_domain.usecase.NotificationsDomainUseCases
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@Singleton
abstract class ApplicationComponent(
    @get:Provides val context: Context,
) : CatalogDomainComponent,
    CatalogDataComponent,
    NotificationsDataComponent,
    NotificationsDomainComponent {

    // Proto datastore
    companion object {
        private const val DATA_STORE_FILE_NAME = "preferences_data.pb"
    }


    @Singleton
    @Provides
    fun provideProtoDataStore(
        appContext: Context = context
    ): DataStore<UserPreferences> = DataStoreFactory.create(
        serializer = PreferencesDataSerializer,
        produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) }
    )

    @Singleton
    @Provides
    fun providePreferences(dataStore: DataStore<UserPreferences>): Preferences =
        DefaultPreferences(dataStore)


    abstract val notificationsDomainUseCases: NotificationsDomainUseCases

    @Provides
    @Singleton
    fun provideRealmDatabase(): Realm {

        /*        val migration = AutomaticSchemaMigration { migrationContext ->
                    migrationContext.enumerate("RealmPlant") { oldObject: DynamicRealmObject, newObject: DynamicMutableRealmObject? ->
                        newObject?.run {
                            // Rename property
                            set("id", oldObject.getValue<ObjectId>("_id"))
                        }
                    }
                }*/

        return Realm.open(
            configuration = RealmConfiguration
                .Builder(
                    schema = setOf(
                        RealmPlant::class
                    )
                )
                .schemaVersion(4)
                .deleteRealmIfMigrationNeeded()
                //.migration(migration)
                .build()
        )
    }
}

interface ApplicationComponentProvider {
    val component: ApplicationComponent
}

val Context.applicationComponent get() = (applicationContext as ApplicationComponentProvider).component