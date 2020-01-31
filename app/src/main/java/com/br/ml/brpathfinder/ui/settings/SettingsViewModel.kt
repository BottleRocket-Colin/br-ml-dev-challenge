package com.br.ml.brpathfinder.ui.settings

data class SettingsViewModel(
    var vibrateRadioButtonChecked: Boolean = false,
    var soundRadioButtonChecked: Boolean = false,
    var bothVibrateAndSoundButtonChecked: Boolean = true,
    var noFeedbackButtonChecked: Boolean = false
)
