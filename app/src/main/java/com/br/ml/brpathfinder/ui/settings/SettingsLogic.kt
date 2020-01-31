package com.br.ml.brpathfinder.ui.settings

import android.content.SharedPreferences
import android.util.Log
import com.br.ml.brpathfinder.ui.settings.SettingsFragment.FeedbackType
import com.br.ml.brpathfinder.ui.settings.SettingsFragment.FeedbackType.*

class SettingsLogic(
    private val listener: Listener
) {

    var viewModel: SettingsViewModel? = null

    interface Listener {
        fun present(vm: SettingsViewModel?)
        fun setInSharedPrefs(feedbackType: FeedbackType)
    }

    fun setUp(sharedPreferences: SharedPreferences?) {
        if (viewModel == null) {
            getFeedbackSetting(sharedPreferences)
            listener.present(viewModel)
        }
    }

    private fun getFeedbackSetting(sharedPreferences: SharedPreferences?) {
        if (sharedPreferences != null) {
            val feedbackSetting = sharedPreferences.getInt(
                "FEEDBACK_TYPE", VIBRATE.ordinal
            )
            Log.d("feedback_option", "Feedback type of $feedbackSetting")

            setViewModel(feedbackSetting)

        } else {
            // we shouldn't be here
        }
    }

    private fun setViewModel(feedbackSetting: Int) {
        when (feedbackSetting) {
            1 -> {
                viewModel?.apply {
                    vibrateRadioButtonChecked = true
                    soundRadioButtonChecked = false
                    bothVibrateAndSoundButtonChecked = false
                    noFeedbackButtonChecked = false
                }
            }
            2 -> {
                viewModel?.apply {
                    vibrateRadioButtonChecked = false
                    soundRadioButtonChecked = true
                    bothVibrateAndSoundButtonChecked = false
                    noFeedbackButtonChecked = false
                }
            }
            3 -> {
                viewModel?.apply {
                    vibrateRadioButtonChecked = false
                    soundRadioButtonChecked = false
                    bothVibrateAndSoundButtonChecked = true
                    noFeedbackButtonChecked = false
                }
            }
            else -> {
                viewModel?.apply {
                    vibrateRadioButtonChecked = false
                    soundRadioButtonChecked = false
                    bothVibrateAndSoundButtonChecked = false
                    noFeedbackButtonChecked = true
                }
            }
        }
    }

    fun onVibrateRadioChecked(isChecked: Boolean) {
        viewModel?.apply {
            vibrateRadioButtonChecked = isChecked
            soundRadioButtonChecked = !isChecked
            bothVibrateAndSoundButtonChecked = !isChecked
            noFeedbackButtonChecked = !isChecked
        }
        Log.d("SettingsLogic", "onVibrateRadioChecked")
        listener.present(viewModel)
        listener.setInSharedPrefs(VIBRATE)
    }

    fun onSoundRadioChecked(isChecked: Boolean) {
        viewModel?.apply {
            vibrateRadioButtonChecked = !isChecked
            soundRadioButtonChecked = isChecked
            bothVibrateAndSoundButtonChecked = !isChecked
            noFeedbackButtonChecked = !isChecked
        }
        Log.d("SettingsLogic", "onSoundRadioChecked")
        listener.present(viewModel)
        listener.setInSharedPrefs(SOUND)
    }

    fun onBothVibrateAndSoundRadioChecked(isChecked: Boolean) {
        viewModel?.apply {
            vibrateRadioButtonChecked = !isChecked
            soundRadioButtonChecked = !isChecked
            bothVibrateAndSoundButtonChecked = isChecked
            noFeedbackButtonChecked = !isChecked
        }
        Log.d("SettingsLogic", "onBothVibrateAndSoundRadioChecked")
        listener.present(viewModel)
        listener.setInSharedPrefs(BOTHVIBRATEANDSOUND)
    }

    fun onNoFeedbackRadioChecked(isChecked: Boolean) {
        viewModel?.apply {
            vibrateRadioButtonChecked = !isChecked
            soundRadioButtonChecked = !isChecked
            bothVibrateAndSoundButtonChecked = !isChecked
            noFeedbackButtonChecked = isChecked
        }
        Log.d("SettingsLogic", "onNoFeedbackRadioChecked")
        listener.present(viewModel)
        listener.setInSharedPrefs(NOFEEDBACK)
    }
}