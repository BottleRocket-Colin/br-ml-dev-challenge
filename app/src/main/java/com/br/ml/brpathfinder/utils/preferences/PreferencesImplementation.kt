package com.br.ml.brpathfinder.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import com.br.ml.pathfinder.domain.utils.PreferencesInterface
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PreferencesImplementation: PreferencesInterface, KoinComponent {
    // DI
    private val context: Context by inject()

    companion object {
        private const val SharedPreferencesKey = "settings.settings_feedback_key"
    }

    private val preferences: SharedPreferences?
        get() = context.getSharedPreferences(
            SharedPreferencesKey,
            Context.MODE_PRIVATE
        )

    override var vibrationEnabled: Boolean by BooleanPreferenceDelegate(preferences, true)
    override var completedOnboarding: Boolean by BooleanPreferenceDelegate(preferences)
}