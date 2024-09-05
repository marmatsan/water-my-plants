package com.marmatsan.dev.watermyplants

import android.app.Application
import com.marmatsan.dev.watermyplants.di.ApplicationComponent
import com.marmatsan.dev.watermyplants.di.ApplicationComponentProvider
import com.marmatsan.dev.watermyplants.di.create

class WaterMyPlantsApplication : Application(), ApplicationComponentProvider {
    override val component by lazy(LazyThreadSafetyMode.NONE) {
        ApplicationComponent::class.create(applicationContext)
    }
}