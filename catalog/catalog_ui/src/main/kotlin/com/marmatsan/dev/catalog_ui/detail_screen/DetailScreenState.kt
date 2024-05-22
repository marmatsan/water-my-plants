package com.marmatsan.dev.catalog_ui.detail_screen

import com.marmatsan.dev.catalog_domain.model.Plant

data class DetailScreenState(
    val plant: Plant = Plant(),
    val isDropDownMenuVisible: Boolean = false
)