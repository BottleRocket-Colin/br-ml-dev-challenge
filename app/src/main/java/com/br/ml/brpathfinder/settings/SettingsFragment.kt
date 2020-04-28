package com.br.ml.brpathfinder.settings

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.settings.SettingsFragment.FeedbackOption.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_activity.*

class SettingsFragment : Fragment(), OnItemSelectedListener {

    private var mediaPlayer: MediaPlayer = MediaPlayer()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val vibrateSwitch: Switch = view.findViewById(R.id.settings_feedback_vibrate_switch)
        val soundSwitch: Switch = view.findViewById(R.id.settings_feedback_sound_switch)
        val vibrateImage: ImageView = view.findViewById(R.id.vibrate_icon_image_view)
        val soundImage: ImageView = view.findViewById(R.id.sound_icon_image_view)

        val alertToneSpinner: Spinner = view.findViewById(R.id.settings_feedback_alert_tone_spinner)
        val buttonLeft: Button = view.findViewById(R.id.settings_feedback_sound_test_left_side)
        val buttonCenter: Button = view.findViewById(R.id.settings_feedback_sound_test_center)
        val buttonRight: Button = view.findViewById(R.id.settings_feedback_sound_test_right_side)

        setUpOptionsFromSharedPrefs(vibrateSwitch, soundSwitch, vibrateImage, soundImage)

        // Switch controlling vibration feedback
        vibrateSwitch.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                vibrateImage.apply {
                    // Change icon color to accent color to show active
                    setAsAccentColor(isChecked)
                    // Shake the icon because why not
                    startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                    // Create a vibration
                    this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            } else {
                vibrateImage.setAsAccentColor(isChecked)
            }

            when {
                isChecked && !soundSwitch.isChecked -> {
                    // Only vibrate is selected
                    // Save the users setting in SharedPreferences
                    saveFeebackOptionsToSharedPrefs(VIBRATE)
                }
                isChecked && soundSwitch.isChecked -> {
                    // Both vibrate and sound is selected
                    // Save the users setting in SharedPreferences
                    saveFeebackOptionsToSharedPrefs(BOTH)
                }
                !isChecked && soundSwitch.isChecked -> {
                    // No longer vibrate but sound is still active
                    // Save the users setting in SharedPreferences
                    saveFeebackOptionsToSharedPrefs(SOUND)
                }
                else -> {
                    // Nothing is selected
                    // Save the users setting in SharedPreferences
                    saveFeebackOptionsToSharedPrefs(NONE)
                }
            }
        }

        // Switch controlling sound feedback
        soundSwitch.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                soundImage.apply {
                    // Change icon color to accent color to show active
                    setAsAccentColor(isChecked)
                    // Shake the icon because why not
                    startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                    // Create a mediaPlayer to play the beep when the sound is enabled
                    val mediaPlayer =
                        MediaPlayer.create(context, pullAlertToneFromSharedPreferences(activity))
                    // Play the beep
                    mediaPlayer.start()
                }
            } else {
                soundImage.setAsAccentColor(isChecked)
            }

            when {
                isChecked && !vibrateSwitch.isChecked -> {
                    // Only sound is selected
                    // Save the users setting in SharedPreferences
                    saveFeebackOptionsToSharedPrefs(SOUND)
                }
                isChecked && vibrateSwitch.isChecked -> {
                    // Both vibrate and sound is selected
                    // Save the users setting in SharedPreferences
                    saveFeebackOptionsToSharedPrefs(BOTH)
                }
                !isChecked && vibrateSwitch.isChecked -> {
                    // No longer sound but vibrate is still active
                    // Save the users setting in SharedPreferences
                    saveFeebackOptionsToSharedPrefs(VIBRATE)
                }
                else -> {
                    // Nothing is selected
                    // Save the users setting in SharedPreferences
                    saveFeebackOptionsToSharedPrefs(NONE)
                }
            }
        }

        // Spinner controlling options of alert tone
        alertToneSpinner.adapter =
            context?.let {
                ArrayAdapter(
                    it,
                    android.R.layout.simple_list_item_1,
                    AlertTones.values()
                )
            }
        alertToneSpinner.onItemSelectedListener = this

        // Buttons for user to test sound options
        buttonLeft.setOnClickListener {
            mediaPlayer = MediaPlayer.create(context, pullAlertToneFromSharedPreferences(activity))
            mediaPlayer.setVolume(1F, 0F)
            mediaPlayer.start()
        }
        buttonCenter.setOnClickListener {
            MediaPlayer.create(context, pullAlertToneFromSharedPreferences(activity)).start()
        }
        buttonRight.setOnClickListener {
            mediaPlayer = MediaPlayer.create(context, pullAlertToneFromSharedPreferences(activity))
            mediaPlayer.setVolume(0F, 1F)
            mediaPlayer.start()
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
            }
            SOUND -> {
                // Only the sound option should be selected
                vibrateSwitch.isChecked = false
                soundSwitch.isChecked = true
            }
            BOTH -> {
                // Both the vibrate and sound should be selected
                vibrateSwitch.isChecked = true
                soundSwitch.isChecked = true
            }
            NONE, NEWUSER -> {
                // Nothing selected
                vibrateSwitch.isChecked = false
                soundSwitch.isChecked = false
            }
        }

        // Set the icon color based on if the switch is on or off
        vibrateIcon.setAsAccentColor(vibrateSwitch.isChecked)
        soundIcon.setAsAccentColor(soundSwitch.isChecked)
    }

    /*
    *   Save the user selected option in SharedPreferences and shows a confirmation Snackbar, If the
    *   option that is selected already is saved, do not show the Snackbar
    * */
    private fun saveFeebackOptionsToSharedPrefs(feedbackOption: FeedbackOption) {
        val sharedPrefs = activity?.getSharedPreferences(
            FEEDBACK_SHARED_PREF_KEY,
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

        snackBar?.setBackgroundTint(resources.getColor(R.color.colorPrimary))
        if (previouslySavedOption != feedbackOption) {
            // Only show the snackBar if a new option has been selected
            snackBar?.show()
        }
    }

    /*
    *   Possible feedback options, These are controlled by the switches for Vibrate and Sound
    * */
    enum class FeedbackOption(val saveKey: String, val snackBarMessage: String) {
        VIBRATE("vibrate", "Vibration only has been saved"),
        SOUND("sound", "Only sound option has been saved"),
        BOTH("both", "Both vibration and sound has been saved"),
        NONE("none", "No feedback saved"),
        NEWUSER("newUser", "")
    }

    /*
    *   Save the user selected option in SharedPreferences and shows a confirmation Snackbar, If the
    *   option that is selected already is saved, do not show the Snackbar
    * */
    private fun saveSoundOptionToSharedPrefs(alertTone: AlertTones) {
        val sharedPrefs = activity?.getSharedPreferences(
            FEEDBACK_SHARED_PREF_KEY,
            Context.MODE_PRIVATE
        ) ?: return

        // Save selected option to SharedPreferences
        with(sharedPrefs.edit()) {
            putString(ALERT_TONE_KEY, alertTone.saveKey)
            commit()
        }
    }

    /*
    * Possible alert tones the user can select
    * */
    enum class AlertTones(val saveKey: String, val soundFile: Int) {
        Beep("beep", R.raw.alert_beep),
        Jazz("jazz", R.raw.alert_jazz),
        Snippy("snippy", R.raw.alert_snippy),
        Voice("voice", R.raw.alert_voice)
    }

    /*
    *   Extension function to change the color of the icon
    * */
    private fun ImageView.setAsAccentColor(active: Boolean) {
        if (active) {
            this.setColorFilter(resources.getColor(R.color.colorPrimaryPurple, null))
        } else {
            this.setColorFilter(Color.BLACK)
        }
    }

    // required override for spinner
    override fun onNothingSelected(parent: AdapterView<*>?) {
        // empty on purpose
    }

    // What to do when a sound is chosen from the spinner
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedSound: AlertTones = parent?.getItemAtPosition(position) as AlertTones
        saveSoundOptionToSharedPrefs(selectedSound)
        mediaPlayer = MediaPlayer.create(context, selectedSound.soundFile)
        mediaPlayer.start()

        Toast.makeText(context, selectedSound.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    companion object {
        const val FEEDBACK_SHARED_PREF_KEY: String = "settings.settings_feedback_key_"
        const val ALERT_TONE_KEY: String = "settings.settings_alert_tone_key"
        const val FEEDBACK_KEY: String = "settings.feedback_key"

        /*
        *   This can be used throughout the app to verify what the current selected options are
        * */
        fun pullFeedbackOptionFromSharedPreferences(activity: Activity?): FeedbackOption? {
            val sharedPrefs =
                activity?.getSharedPreferences(FEEDBACK_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                    ?: return null

            // Using "NEWUSER" as default
            val savedOptionKey = sharedPrefs.getString(FEEDBACK_KEY, NEWUSER.saveKey)

            // Return the feedback option
            for (option in values()) {
                if (option.saveKey == savedOptionKey) {
                    return option
                }
            }

            // This should not get hit since the default value is set when pulling shared prefs
            return null
        }

        /*
        *  This can be used to pull the users selected alert tone.  This only pulls the sound file
        * */
        fun pullAlertToneFromSharedPreferences(activity: Activity?): Int {
            val sharedPrefs =
                activity?.getSharedPreferences(FEEDBACK_SHARED_PREF_KEY, Context.MODE_PRIVATE)
                    ?: return R.raw.alert_beep

            val savedAlertTone = sharedPrefs.getString(ALERT_TONE_KEY, AlertTones.Beep.saveKey)

            for (tones in AlertTones.values()) {
                if (tones.saveKey == savedAlertTone) {
                    return tones.soundFile
                }
            }

            // This should not get hit But if it does, well use Beep as default
            return R.raw.alert_beep
        }
    }
}