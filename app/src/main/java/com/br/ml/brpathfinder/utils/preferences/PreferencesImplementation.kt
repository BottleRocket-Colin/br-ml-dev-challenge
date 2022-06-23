package com.br.ml.brpathfinder.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import com.br.ml.pathfinder.domain.utils.PreferencesInterface

class PreferencesImplementation(private val context: Context) : PreferencesInterface {
    companion object {
        private const val SharedPreferencesKey = "settings.settings_feedback_key"
    }

    private val preferences: SharedPreferences?
        get() = context.getSharedPreferences(
            SharedPreferencesKey,
            Context.MODE_PRIVATE
        )

//    override var currentFeedbackMode: String by StringPreferenceDelegate(preferences)
//    override var pitchAdjustModeActive: Boolean by BooleanPreferenceDelegate(preferences, true)
    override var completedOnboarding: Boolean by BooleanPreferenceDelegate(preferences)
}