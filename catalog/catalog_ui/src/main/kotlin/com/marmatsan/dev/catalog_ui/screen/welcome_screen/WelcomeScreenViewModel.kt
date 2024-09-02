package com.marmatsan.dev.catalog_ui.screen.welcome_screen

import com.marmatsan.dev.core_ui.viewmodel.MviViewModel
import me.tatarka.inject.annotations.Inject

@Inject
class WelcomeScreenViewModel : MviViewModel<WelcomeScreenAction, WelcomeScreenEvent>() {

    override fun handleAction(
        action: WelcomeScreenAction
    ) {
        when (action) {
            is WelcomeScreenAction.OnAddFirstPlant -> {
                sendEvent(WelcomeScreenEvent.Navigate)
            }
        }
    }
}

