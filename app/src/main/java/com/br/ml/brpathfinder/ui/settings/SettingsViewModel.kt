package com.br.ml.brpathfinder.ui.settings

import com.br.ml.brpathfinder.ui.BaseViewModel
import com.br.ml.pathfinder.domain.utils.PreferencesInterface
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.inject

class SettingsViewModel: BaseViewModel() {
    // DI
    private val preferences: PreferencesInterface by inject()

    // UI
    val vibrationActive = MutableStateFlow<Boolean>(preferences.vibrationEnabled)

    init {
        launchIO {
            vibrationActive.collect {
                preferences.vibrationEnabled = it
            }
        }
    }
}