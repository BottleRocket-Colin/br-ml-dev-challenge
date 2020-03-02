package com.br.ml.brpathfinder.settings

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.br.ml.brpathfinder.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.Serializable

class SettingsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val vibrateOption: RadioButton = view.findViewById(R.id.settings_feedback_vibrate_radio_button)
        val soundOption: RadioButton = view.findViewById(R.id.settings_feedback_sound_radio_button)
        val soundAndVibrateOption: RadioButton = view.findViewById(R.id.settings_feedback_vibrate_and_sound_radio_button)
        val noFeedbackOption: RadioButton = view.findViewById(R.id.settings_feedback_no_feedback_radio_button)


        setUpOptionsFromSharedPrefs(vibrateOption, soundOption, soundAndVibrateOption, noFeedbackOption)

        vibrateOption.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                vibrateOptionSelected(buttonView, isChecked)
            }
        }
        soundOption.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                soundOptionSelected(buttonView, isChecked)
            }
        }
        soundAndVibrateOption.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                vibrateAndSoundOptionSelected(buttonView, isChecked)
            }
        }
        noFeedbackOption.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                noOptionSelected(buttonView, isChecked)
            }
        }

        return view
    }

    private fun setUpOptionsFromSharedPrefs(
            vibrateOption: RadioButton,
            soundOption: RadioButton,
            soundAndVibrateOption: RadioButton,
            noFeedbackOption: RadioButton
    ) {
        when (pullFromSharedPrefs()) {
            FeedbackOption.VIBRATE -> {
                // Show correct radio button checked
                vibrateOption.isChecked = true
                soundOption.isChecked = false
                soundAndVibrateOption.isChecked = false
                noFeedbackOption.isChecked = false

            }
            FeedbackOption.SOUND -> {
                // Show correct radio button checked
                vibrateOption.isChecked = false
                soundOption.isChecked = true
                soundAndVibrateOption.isChecked = false
                noFeedbackOption.isChecked = false
            }
            FeedbackOption.BOTH -> {
                // Show correct radio button checked
                vibrateOption.isChecked = false
                soundOption.isChecked = false
                soundAndVibrateOption.isChecked = true
                noFeedbackOption.isChecked = false
            }
            FeedbackOption.NONE -> {
                // Show correct radio button checked
                vibrateOption.isChecked = false
                soundOption.isChecked = false
                soundAndVibrateOption.isChecked = false
                noFeedbackOption.isChecked = true
            }
        }
    }

    private fun vibrateOptionSelected(buttonView: CompoundButton?, checked: Boolean) {
        // Uncheck other radio buttons
        settings_feedback_vibrate_radio_button.isChecked = checked
        settings_feedback_sound_radio_button.isChecked = !checked
        settings_feedback_vibrate_and_sound_radio_button.isChecked = !checked
        settings_feedback_no_feedback_radio_button.isChecked = !checked

        // Save the users setting in SharedPreferences
        setInSharedPreferences(buttonView, FeedbackOption.VIBRATE)
    }

    private fun soundOptionSelected(buttonView: CompoundButton?, checked: Boolean) {
        // Uncheck other radio buttons
        settings_feedback_vibrate_radio_button.isChecked = !checked
        settings_feedback_sound_radio_button.isChecked = checked
        settings_feedback_vibrate_and_sound_radio_button.isChecked = !checked
        settings_feedback_no_feedback_radio_button.isChecked = !checked

        // Save the users setting in SharedPreferences
        setInSharedPreferences(buttonView, FeedbackOption.SOUND)
    }

    private fun vibrateAndSoundOptionSelected(buttonView: CompoundButton?, checked: Boolean) {
        // Uncheck other radio buttons
        settings_feedback_vibrate_radio_button.isChecked = !checked
        settings_feedback_sound_radio_button.isChecked = !checked
        settings_feedback_vibrate_and_sound_radio_button.isChecked = checked
        settings_feedback_no_feedback_radio_button.isChecked = !checked

        // Save the users setting in SharedPreferences
        setInSharedPreferences(buttonView, FeedbackOption.BOTH)
    }

    private fun noOptionSelected(buttonView: CompoundButton?, checked: Boolean) {
        // Uncheck other radio buttons
        settings_feedback_vibrate_radio_button.isChecked = !checked
        settings_feedback_sound_radio_button.isChecked = !checked
        settings_feedback_vibrate_and_sound_radio_button.isChecked = !checked
        settings_feedback_no_feedback_radio_button.isChecked = checked

        // Save the users setting in SharedPreferences
        setInSharedPreferences(buttonView, FeedbackOption.NONE)
    }

    private fun setInSharedPreferences(buttonView: CompoundButton?, feedbackOption: FeedbackOption) {
        val sharedPrefs = activity?.getSharedPreferences(
                getString(R.string.settings_preference_key),
                Context.MODE_PRIVATE
        ) ?: return

        // Get previously saved option, in future we could have this also "undo" the change
        val previouslySavedOption = pullFromSharedPrefs()

        // Save selected option to SharedPreferences
        with(sharedPrefs.edit()) {
            putString(FEEDBACK_KEY, feedbackOption.preferenceKey)
            commit()
        }

        // Snack bar to tell the user that the selected option was saved
        val rootView = buttonView?.rootView ?: return
        val snackBarMessage = if (feedbackOption != previouslySavedOption) {
            "${feedbackOption.name} has been saved"
        } else {
            "${feedbackOption.name} is already saved"
        }
        val snackBar = Snackbar.make(rootView, snackBarMessage, Snackbar.LENGTH_SHORT)
        snackBar.setTextColor(Color.WHITE)
        snackBar.setBackgroundTint(Color.BLUE)
        snackBar.show()
    }

    private fun pullFromSharedPrefs(): FeedbackOption? {
        val sharedPrefs = activity?.getSharedPreferences(
                getString(R.string.settings_preference_key),
                Context.MODE_PRIVATE
        ) ?: return null
        // Currently I have vibrate as default, but we can change it to any options
        val savedOptionKey = sharedPrefs.getString(FEEDBACK_KEY, "vibrate")

        // Return the feedback option
        for (option in FeedbackOption.values()) {
            if (option.preferenceKey == savedOptionKey) {
                return option
            }
        }

        // This should not get hit
        return null
    }

    companion object {
        private const val FEEDBACK_KEY: String = "settings.feedback_key"

        @JvmStatic
        fun newInstance() =
                SettingsFragment()
    }

    enum class FeedbackOption(val preferenceKey: String) : Serializable {
        VIBRATE("vibrate"),
        SOUND("sound"),
        BOTH("both"),
        NONE("none")
    }
}