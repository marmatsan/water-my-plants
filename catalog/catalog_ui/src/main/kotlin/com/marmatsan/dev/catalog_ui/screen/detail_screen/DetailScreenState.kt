package com.marmatsan.dev.catalog_ui.screen.detail_screen

import com.marmatsan.dev.catalog_domain.model.Plant

data class DetailScreenState(
    val plant: Plant? = null,
    val isLoadingPlant: Boolean = true,
    val isDropdownMenuVisible: Boolean = false
)