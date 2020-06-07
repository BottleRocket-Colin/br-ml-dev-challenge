package com.br.ml.brpathfinder.utils.preferences

interface PreferencesInterface {
    var currentFeedbackMode: String
    var currentAlertToneSaveKey: String
    var noHeadphoneModeActive: Boolean
    var pitchAdjustModeActive: Boolean
    var completedOnboarding: Boolean
}