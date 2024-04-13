package com.marmatsan.dev.watermyplants.di

import android.app.Application

class WaterMyPlantsApplication : Application(), ApplicationComponentProvider {
    override val component by lazy(LazyThreadSafetyMode.NONE) {
        ApplicationComponent::class.create(applicationContext)
    }
}