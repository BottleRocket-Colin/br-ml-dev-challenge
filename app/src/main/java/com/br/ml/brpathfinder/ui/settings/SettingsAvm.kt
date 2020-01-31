package com.br.ml.brpathfinder.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope

class SettingsAvm : ViewModel() {

    private val settingsLogic: SettingsLogic

    private val presenter = MutableLiveData<SettingsViewModel>()
    private val setInSharedPrefs = MutableLiveData<SettingsFragment.FeedbackType>()

    init {
        val listener = object : SettingsLogic.Listener {
            override fun present(vm: SettingsViewModel?) = presenter.postValue(vm)
            override fun setInSharedPrefs(feedbackType: SettingsFragment.FeedbackType) = setInSharedPrefs.postValue(feedbackType)
        }
        settingsLogic = SettingsLogic(listener)
    }

    fun presenter() = presenter
    fun setInSharedPrefs() = setInSharedPrefs

    fun setUp(sharedPreferences: SharedPreferences?) = settingsLogic.setUp(sharedPreferences)
    fun onVibrateRadioChecked(isChecked: Boolean) = settingsLogic.onVibrateRadioChecked(isChecked)
    fun onSoundRadioChecked(isChecked: Boolean) = settingsLogic.onSoundRadioChecked(isChecked)
    fun onBothVibrateAndSoundRadioChecked(isChecked: Boolean) = settingsLogic.onBothVibrateAndSoundRadioChecked(isChecked)
    fun onNoFeedbackRadioChecked(isChecked: Boolean) = settingsLogic.onNoFeedbackRadioChecked(isChecked)
}