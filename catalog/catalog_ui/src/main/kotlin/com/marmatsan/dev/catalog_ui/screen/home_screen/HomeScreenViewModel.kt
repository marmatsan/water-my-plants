package com.marmatsan.dev.catalog_ui.screen.home_screen

import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.core_ui.viewmodel.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class HomeScreenViewModel(
    private val repository: CatalogRepository
) : MviViewModel<HomeScreenAction, HomeScreenEvent>() {

    private val homeScreenStateFlow = MutableStateFlow(HomeScreenState())

    init {
        viewModelScope.launch {
            repository.readAllPlantsFlow().collectLatest { plants ->
                homeScreenStateFlow.update {
                    it.copy(plants = plants)
                }
            }
        }
    }

    val state = homeScreenStateFlow.asStateFlow()

    override fun handleAction(action: HomeScreenAction) {
        when (action) {
            is HomeScreenAction.OnCreatePlant ->
                sendEvent(HomeScreenEvent.NavigateToPlantScreen)
            is HomeScreenAction.OnNavigateToDetailScreen ->
                sendEvent(HomeScreenEvent.NavigateToDetailScreen(action.plantId))
        }
    }

}