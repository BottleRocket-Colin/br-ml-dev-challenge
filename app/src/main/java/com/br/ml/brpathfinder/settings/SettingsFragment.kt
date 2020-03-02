package com.br.ml.brpathfinder.settings

import android.app.Activity
import android.app.LauncherActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.br.ml.brpathfinder.MainActivity
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.settings.SettingsFragment.FeedbackOption.*
import com.br.ml.brpathfinder.ui.LandingActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_activity.*

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val vibrateSwitch: Switch = view.findViewById(R.id.settings_feedback_vibrate_switch)
        val soundSwitch: Switch = view.findViewById(R.id.settings_feedback_sound_switch)
        val backButton: Button = view.findViewById(R.id.settings_back_button)

        setUpOptionsFromSharedPrefs(vibrateSwitch, soundSwitch)

        // Switch controlling vibration feedback
        vibrateSwitch.setOnCheckedChangeListener { _, isChecked ->
            when {
                isChecked && !soundSwitch.isChecked -> {
                    // Only vibrate is selected
                    // Save the users setting in SharedPreferences
                    setInSharedPreferences(VIBRATE)
                }
                isChecked && soundSwitch.isChecked -> {
                    // Both vibrate and sound is selected
                    // Save the users setting in SharedPreferences
                    setInSharedPreferences(BOTH)
                }
                !isChecked && soundSwitch.isChecked -> {
                    // No longer vibrate but sound is still active
                    // Save the users setting in SharedPreferences
                    setInSharedPreferences(SOUND)
                }
                else -> {
                    // Nothing is selected
                    // Save the users setting in SharedPreferences
                    setInSharedPreferences(NONE)
                }
            }
        }

        // Switch controlling sound feedback
        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            when {
                isChecked && !vibrateSwitch.isChecked -> {
                    // Only sound is selected
                    // Save the users setting in SharedPreferences
                    setInSharedPreferences(SOUND)
                }
                isChecked && vibrateSwitch.isChecked -> {
                    // Both vibrate and sound is selected
                    // Save the users setting in SharedPreferences
                    setInSharedPreferences(BOTH)
                }
                !isChecked && vibrateSwitch.isChecked -> {
                    // No longer sound but vibrate is still active
                    // Save the users setting in SharedPreferences
                    setInSharedPreferences(VIBRATE)
                }
                else -> {
                    // Nothing is selected
                    // Save the users setting in SharedPreferences
                    setInSharedPreferences(NONE)
                }
            }
        }

        // Temporary back button
        backButton.setOnClickListener {
            backButtonPressed()
        }

        return view
    }

    /*
    *   Set the switches to be checked based on what the saved options are
    *  */
    private fun setUpOptionsFromSharedPrefs(
        vibrateOption: Switch,
        soundOption: Switch
    ) {
        when (pullFeedbackOptionFromSharedPreferences(activity)) {
            VIBRATE -> {
                // Only the vibrate option should be selected
                vibrateOption.isChecked = true
                soundOption.isChecked = false
            }
            SOUND -> {
                // Only the sound option should be selected
                vibrateOption.isChecked = false
                soundOption.isChecked = true
            }
            BOTH -> {
                // Both the vibrate and sound should be selected
                vibrateOption.isChecked = true
                soundOption.isChecked = true
            }
            NONE -> {
                // Nothing selected
                vibrateOption.isChecked = false
                soundOption.isChecked = false
            }
        }
    }

    /*
    *   Save the user selected option in SharedPreferences and shows a confirmation Snackbar, If the
    *   option that is selected already is saved, do not show the Snackbar
    * */
    private fun setInSharedPreferences(feedbackOption: FeedbackOption) {
        val sharedPrefs = activity?.getSharedPreferences(
            SHARED_PREF_KEY,
            Context.MODE_PRIVATE
        ) ?: return

        // Get previously saved option, in future we could have this also "undo" the change
        val previouslySavedOption = pullFeedbackOptionFromSharedPreferences(activity)

        // Save selected option to SharedPreferences
        with(sharedPrefs.edit()) {
            putString(FEEDBACK_KEY, feedbackOption.saveKey)
            commit()
        }

        // Snack bar to tell the user that the selected option was saved
        val snackBar =
            activity?.container?.let {
                Snackbar.make(
                    it,
                    feedbackOption.snackBarMessage,
                    Snackbar.LENGTH_SHORT
                )
            }
        snackBar?.setTextColor(Color.WHITE)
        snackBar?.setBackgroundTint(Color.BLUE)
        if (previouslySavedOption != feedbackOption) {
            // Only show the snackBar if a new option has been selected
            snackBar?.show()
        }
    }

    /*
    *   Possible feedback optionsThese are controlled by the switches for Vibrate and Sound
    * */
    enum class FeedbackOption(val saveKey: String, val snackBarMessage: String) {
        VIBRATE("vibrate", "Only vibrate feedback option has been saved"),
        SOUND("sound", "Only sound option has been saved"),
        BOTH("both", "Both vibrate and sound have been saved"),
        NONE("none", "No feedback has been saved")
    }

    companion object {
        const val SHARED_PREF_KEY: String = "settings.settings_feedback_key"
        const val FEEDBACK_KEY: String = "settings.feedback_key"

        @JvmStatic
        fun newInstance() =
            SettingsFragment()

        /*
        *   This can be used throughout the app to verify what the current selected options are
        * */
        fun pullFeedbackOptionFromSharedPreferences(activity: Activity?): FeedbackOption? {
            val sharedPrefs =
                activity?.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
                    ?: return null

            // Currently I have vibrate as default, but we can change it to any options
            val savedOptionKey = sharedPrefs.getString(FEEDBACK_KEY, "vibrate")

            // Return the feedback option
            for (option in FeedbackOption.values()) {
                if (option.saveKey == savedOptionKey) {
                    return option
                }
            }

            // This should not get hit since the default value is set when pulling shared prefs
            return null
        }

    }

    // Temporary way to get back to the landing page, will change when we have a more concrete flow
    private fun backButtonPressed() {
        val intent = Intent(context, LandingActivity::class.java)
        startActivity(intent)
    }
}