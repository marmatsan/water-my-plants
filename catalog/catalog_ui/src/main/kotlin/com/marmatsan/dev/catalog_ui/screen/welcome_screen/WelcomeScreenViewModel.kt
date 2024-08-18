package com.marmatsan.dev.catalog_ui.screen.welcome_screen

import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.core_domain.preferences.Preferences
import com.marmatsan.dev.core_domain.updateState
import com.marmatsan.dev.core_ui.viewmodel.MVIViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class WelcomeScreenViewModel(
    private val preferences: Preferences
) : MVIViewModel<WelcomeScreenAction, WelcomeScreenEvent>() {
    private val welcomeScreenMutableStateFlow = MutableStateFlow(WelcomeScreenState())
    val state = welcomeScreenMutableStateFlow.asStateFlow()

    override fun handleAction(
        action: WelcomeScreenAction
    ) {
        when (action) {
            is WelcomeScreenAction.OnAddFirstPlant -> {
                viewModelScope.launch {
                    val isMediaAccessPermanentlyDeclined =
                        preferences.readIsMediaAccessPermanentlyDeclined()

                    if (isMediaAccessPermanentlyDeclined)
                        sendEvent(WelcomeScreenEvent.Navigate)
                    else {
                        sendEvent(WelcomeScreenEvent.RequestMediaPermission)
                    }
                }
            }

            is WelcomeScreenAction.GrantMediaPermission -> {
                viewModelScope.launch {
                    preferences.saveIsMediaAccessPermanentlyDeclined(declined = false)
                    welcomeScreenMutableStateFlow.updateState {
                        copy(isRequestMediaPermissionDialogVisible = false)
                    }
                }
                sendEvent(WelcomeScreenEvent.RequestMediaPermission)
            }

            is WelcomeScreenAction.PermanentlyDeclineMediaPermission -> {
                viewModelScope.launch {
                    preferences.saveIsMediaAccessPermanentlyDeclined(declined = true)
                    welcomeScreenMutableStateFlow.updateState {
                        copy(isRequestMediaPermissionDialogVisible = false)
                    }
                }
                sendEvent(WelcomeScreenEvent.Navigate)
            }
        }
    }

    fun showRequestMediaPermissionDialog() = welcomeScreenMutableStateFlow.updateState {
        copy(isRequestMediaPermissionDialogVisible = true)
    }
}

