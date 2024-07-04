package com.marmatsan.dev.watermyplants.di

import android.content.Context
import com.marmatsan.catalog_data.di.CatalogDataComponent
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.dev.catalog_domain.di.CatalogDomainComponent
import com.marmatsan.dev.core_domain.di.Singleton
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