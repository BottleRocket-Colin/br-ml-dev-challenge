package com.br.ml.brpathfinder.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.br.ml.pathfinder.compose.ui.settings.SettingsState

@Composable
fun SettingsViewModel.toState() = SettingsState(
    vibrateActive = vibrationActive.collectAsState(),
    onVibrateChanged = {
        vibrationActive.value = it
    }
)