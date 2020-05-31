package com.br.ml.brpathfinder.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import com.br.ml.brpathfinder.settings.SettingsFragment

class PreferencesImplementation(private val context: Context) : PreferencesInterface {
    companion object {
        private const val SharedPreferencesKey = "settings.settings_feedback_key_"
    }

    private val preferences: SharedPreferences?
        get() = context.getSharedPreferences(
            SharedPreferencesKey,
            Context.MODE_PRIVATE
        )

    override var currentFeedbackMode: String by StringPreferenceDelegate(preferences)
    override var currentAlertToneSaveKey: String by StringPreferenceDelegate(preferences)
    override var noHeadphoneModeActive: Boolean by BooleanPreferenceDelegate(preferences)
    override var completedOnboarding: Boolean by BooleanPreferenceDelegate(preferences)
}