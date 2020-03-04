package com.br.ml.brpathfinder.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import androidx.fragment.app.Fragment
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
        val vibrateImage: ImageView = view.findViewById(R.id.vibrate_icon_image_view)
        val soundImage: ImageView = view.findViewById(R.id.sound_icon_image_view)

        setUpOptionsFromSharedPrefs(vibrateSwitch, soundSwitch, vibrateImage, soundImage)

        // Switch controlling vibration feedback
        vibrateSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                vibrateImage.apply {
                    // Change icon color to accent color to show active
                    setAsAccentColor()
                    // Shake the icon because why not
                    startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                }

            } else {
                vibrateImage.setColorFilter(Color.BLACK)
            }
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
            if (isChecked) {
                soundImage.apply {
                    // Change icon color to accent color to show active
                    setAsAccentColor()
                    // Shake the icon because why not
                    startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                }
            } else {
                soundImage.setColorFilter(Color.BLACK)
            }
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
        vibrateSwitch: Switch,
        soundSwitch: Switch,
        vibrateIcon: ImageView,
        soundIcon: ImageView
    ) {
        when (pullFeedbackOptionFromSharedPreferences(activity)) {
            VIBRATE -> {
                // Only the vibrate option should be selected
                vibrateSwitch.isChecked = true
                soundSwitch.isChecked = false
                vibrateIcon.setAsAccentColor()
                soundIcon.setColorFilter(Color.BLACK)
            }
            SOUND -> {
                // Only the sound option should be selected
                vibrateSwitch.isChecked = false
                soundSwitch.isChecked = true
                vibrateIcon.setColorFilter(Color.BLACK)
                soundIcon.setAsAccentColor()
            }
            BOTH -> {
                // Both the vibrate and sound should be selected
                vibrateSwitch.isChecked = true
                soundSwitch.isChecked = true
                vibrateIcon.setAsAccentColor()
                soundIcon.setAsAccentColor()
            }
            NONE -> {
                // Nothing selected
                vibrateSwitch.isChecked = false
                soundSwitch.isChecked = false
                vibrateIcon.setColorFilter(Color.BLACK)
                soundIcon.setColorFilter(Color.BLACK)
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
        VIBRATE("vibrate", "Shhh... only vibration"),
        SOUND("sound", "Only sound option has been saved"),
        BOTH("both", "\uD83D\uDDE3️ &️ \uD83D\uDCF3"),
        NONE("none", "Feedback inactive")
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
            for (option in values()) {
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

    /*
    *   Extension function to change the color of the icon
    * */
    private fun ImageView.setAsAccentColor() {
        this.setColorFilter(resources.getColor(R.color.colorAccent, null))
    }
}