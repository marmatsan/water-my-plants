package com.marmatsan.dev.catalog_ui.screen.home_screen

import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.core_ui.viewmodel.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject

@Inject
class HomeScreenViewModel(
    private val repository: CatalogRepository
) : MviViewModel<HomeScreenAction, HomeScreenEvent>() {

    private val homeScreenStateFlow = MutableStateFlow(HomeScreenState())
    private val plantsListFlow = repository.readAllPlantsFlow()

    val state = combine(
        homeScreenStateFlow,
        plantsListFlow
    ) { homeScreenState, plantsList ->
        homeScreenState.copy(plants = plantsList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = HomeScreenState()
    )

    override fun handleAction(action: HomeScreenAction) {

    }

}