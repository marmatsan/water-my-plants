package com.marmatsan.dev.catalog_ui.screen.home_screen

import com.marmatsan.dev.catalog_domain.model.Plant

data class HomeScreenState(
    val plants: List<Plant>? = null
)
