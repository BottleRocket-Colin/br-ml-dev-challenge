package com.br.ml.brpathfinder.settings

import android.content.Context.AUDIO_SERVICE
import android.graphics.Color
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.settings.SettingsFragment.AlertTone.Beep
import com.br.ml.brpathfinder.settings.SettingsFragment.FeedbackOption.*
import com.br.ml.brpathfinder.utils.preferences.PreferencesImplementation
import com.erkutaras.showcaseview.ShowcaseManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.main_activity.*

class SettingsFragment : Fragment(), OnItemSelectedListener {

    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private lateinit var preferences: PreferencesImplementation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferencesImplementation(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val vibrateSwitch: SwitchMaterial = view.findViewById(R.id.settings_feedback_vibrate_switch)
        val soundSwitch: SwitchMaterial = view.findViewById(R.id.settings_feedback_sound_switch)
        val vibrateImage: ImageView = view.findViewById(R.id.vibrate_icon_image_view)
        vibrateFocus = buildFocus(vibrateImage, "Enable Vibrate")
        val soundImage: ImageView = view.findViewById(R.id.sound_icon_image_view)
        soundFocus = buildFocus(soundImage, "Enable Sound")

        val alertToneSpinner: Spinner = view.findViewById(R.id.settings_feedback_alert_tone_spinner)
        val noHeadphoneModeSwitch: SwitchMaterial =
            view.findViewById(R.id.settings_fragment_no_headphone_mode_switch)
        val noHeadphoneSuggestion: MaterialTextView =
            view.findViewById(R.id.settings_feedback_no_headphone_suggestion)
        val buttonLeft: MaterialButton =
            view.findViewById(R.id.settings_feedback_sound_test_left_side)
        val buttonCenter: MaterialButton =
            view.findViewById(R.id.settings_feedback_sound_test_center)
        val buttonRight: MaterialButton =
            view.findViewById(R.id.settings_feedback_sound_test_right_side)

        setUpOptionsFromPrefs(
            vibrateSwitch,
            soundSwitch,
            vibrateImage,
            soundImage,
            alertToneSpinner,
            noHeadphoneModeSwitch,
            noHeadphoneSuggestion
        )

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
                    saveFeedbackOptionsToSharedPrefs(VIBRATE)
                }
                isChecked && soundSwitch.isChecked -> {
                    saveFeedbackOptionsToSharedPrefs(BOTH)
                }
                !isChecked && soundSwitch.isChecked -> {
                    saveFeedbackOptionsToSharedPrefs(SOUND)
                }
                else -> {
                    saveFeedbackOptionsToSharedPrefs(NONE)
                }
            }
        }

        // Switch controlling sound feedback
        soundSwitch.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                soundImage.apply {
                    // Change the layout based on if the checkbox is checked
                    enableOrDisableSoundSettings(isChecked)
                    // Shake the icon because why not
                    startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                    // Check if headphones are connected, if not, suggest NoHeadphone Mode
                    if (!areHeadphonesConnected()) {
                        noHeadphoneSuggestion.visibility = View.VISIBLE
                    } else {
                        noHeadphoneSuggestion.visibility = View.INVISIBLE
                    }
                    // Create a mediaPlayer to play the beep when the sound is enabled
                    val mediaPlayer =
                        MediaPlayer.create(
                            context,
                            preferences.currentAlertToneSaveKey.convertToAlertTone().soundFile
                        )
                    // Play the beep
                    mediaPlayer.start()
                }
            } else {
                enableOrDisableSoundSettings(isChecked)
            }

            when {
                isChecked && !vibrateSwitch.isChecked -> {
                    saveFeedbackOptionsToSharedPrefs(SOUND)
                }
                isChecked && vibrateSwitch.isChecked -> {
                    saveFeedbackOptionsToSharedPrefs(BOTH)
                }
                !isChecked && vibrateSwitch.isChecked -> {
                    saveFeedbackOptionsToSharedPrefs(VIBRATE)
                }
                else -> {
                    saveFeedbackOptionsToSharedPrefs(NONE)
                }
            }
        }

        // Spinner controlling options of alert tone
        alertToneSpinner.apply {
            adapter = context.run {
                ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    AlertTone.values()
                )
            }
            onItemSelectedListener = this@SettingsFragment
            alertToneSpinner.setSelection(preferences.currentAlertToneSaveKey.convertToAlertTone().ordinal)
        }

        // Switch controlling No Headphone mode
        noHeadphoneModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.noHeadphoneModeActive = isChecked
        }
        alertToneSpinner.adapter =
            context?.let {
                ArrayAdapter(
                    it,
                    android.R.layout.simple_list_item_1,
                    AlertTone.values()
                )
            }
        alertToneSpinner.onItemSelectedListener = this

        // Buttons for user to test sound options
        buttonLeft.setOnClickListener {
            if (!preferences.noHeadphoneModeActive) {
                mediaPlayer =
                    MediaPlayer.create(
                        context,
                        preferences.currentAlertToneSaveKey.convertToAlertTone().soundFile
                    )
                mediaPlayer.setVolume(1F, 0F)
            } else {
                mediaPlayer = MediaPlayer.create(context, R.raw.piano_left)
            }
            mediaPlayer.start()
        }
        buttonCenter.setOnClickListener {
            mediaPlayer = if (!preferences.noHeadphoneModeActive) {
                MediaPlayer.create(
                    context,
                    preferences.currentAlertToneSaveKey.convertToAlertTone().soundFile
                )
            } else {
                MediaPlayer.create(context, R.raw.piano_center)
            }
            mediaPlayer.start()
        }
        buttonRight.setOnClickListener {
            if (!preferences.noHeadphoneModeActive) {
                mediaPlayer =
                    MediaPlayer.create(
                        context,
                        preferences.currentAlertToneSaveKey.convertToAlertTone().soundFile
                    )
                mediaPlayer.setVolume(0F, 1F)
            } else {
                mediaPlayer = MediaPlayer.create(context, R.raw.piano_right)
            }
            mediaPlayer.start()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).apply {
            supportActionBar?.apply {
                this.title = resources.getString(R.string.feedback_settings_title)
            }
        }
    }

    /*
    *   Set the switches to be checked based on what the saved options are
    *  */
    private fun setUpOptionsFromPrefs(
        vibrateSwitch: SwitchMaterial,
        soundSwitch: SwitchMaterial,
        vibrateIcon: ImageView,
        soundIcon: ImageView,
        alertToneSpinner: Spinner,
        noHeadphoneSwitch: SwitchMaterial,
        noHeadphoneSuggestion: TextView
    ) {
        when (preferences.currentFeedbackMode.convertToFeedbackOption()) {
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

        when (preferences.currentAlertToneSaveKey) {
            Beep.saveKey -> {
                alertToneSpinner.prompt = Beep.name
            }
            AlertTone.Jazz.saveKey -> {
                alertToneSpinner.prompt = AlertTone.Jazz.name
            }
            AlertTone.Snippy.saveKey -> {
                alertToneSpinner.prompt = AlertTone.Snippy.name
            }
            AlertTone.Voice.saveKey -> {
                alertToneSpinner.prompt = AlertTone.Voice.name
            }
        }

        noHeadphoneSwitch.isChecked = preferences.noHeadphoneModeActive

        // Set the icon color based on if the switch is on or off
        vibrateIcon.setAsAccentColor(vibrateSwitch.isChecked)
        soundIcon.setAsAccentColor(soundSwitch.isChecked)

        if (!areHeadphonesConnected() || soundSwitch.isChecked) {
            noHeadphoneSuggestion.visibility = View.VISIBLE
        }
    }

    /*
    *  Disable the sound sub-options when the sound switch is turned off
    *  */
    private fun enableOrDisableSoundSettings(isChecked: Boolean) {
        sound_icon_image_view.setAsAccentColor(isChecked)
        settings_feedback_alert_tone_spinner.isEnabled = isChecked
        settings_fragment_no_headphone_mode_switch.isEnabled = isChecked
        settings_feedback_sound_test_left_side.isEnabled = isChecked
        settings_feedback_sound_test_center.isEnabled = isChecked
        settings_feedback_sound_test_right_side.isEnabled = isChecked
        if (!isChecked) {
            settings_feedback_select_alert_tone_title.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.darker_gray
                )
            )
            settings_feedback_no_headphones_mode_title.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.darker_gray
                )
            )
            settings_feedback_sound_test_title.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.darker_gray
                )
            )
            settings_feedback_no_headphone_suggestion.visibility = View.INVISIBLE
        } else {
            settings_feedback_select_alert_tone_title.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.black
                )
            )
            settings_feedback_no_headphones_mode_title.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.black
                )
            )
            settings_feedback_sound_test_title.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.black
                )
            )
        }
    }

    /*
    *   Save the user selected option in SharedPreferences and shows a confirmation Snackbar, If the
    *   option that is selected already is saved, do not show the Snackbar
    * */
    private fun saveFeedbackOptionsToSharedPrefs(feedbackOption: FeedbackOption) {
        // Get previously saved option, in future we could have this also "undo" the change
        val previouslySavedOption = preferences.currentFeedbackMode

        // Save selected option to SharedPreferences
        preferences.currentFeedbackMode = feedbackOption.saveKey

        // Snack bar to tell the user that the selected option was saved
        val snackBar =
            activity?.container?.let {
                Snackbar.make(
                    it,
                    feedbackOption.snackBarMessage,
                    Snackbar.LENGTH_SHORT
                )
            }?.apply {
                setTextColor(Color.WHITE)
                animationMode = Snackbar.ANIMATION_MODE_FADE
                duration = Snackbar.LENGTH_LONG
            }

        snackBar?.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        if (previouslySavedOption != preferences.currentFeedbackMode) {
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
    * Possible alert tones the user can select
    * */
    enum class AlertTone(val saveKey: String, val soundFile: Int) {
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

    /*
    *  Check if the headphones are connected
    * */
    private fun areHeadphonesConnected(): Boolean {
        val audioManager: AudioManager =
            requireContext().getSystemService(AUDIO_SERVICE) as AudioManager
        val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL)

        for (deviceInfo in audioDevices) {
            if (deviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                || deviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                || deviceInfo.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
            ) {
                return true
            }
        }

        return false
    }

    // required override for spinner
    override fun onNothingSelected(parent: AdapterView<*>?) {
        // empty on purpose
    }

    // What to do when a sound is chosen from the spinner
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedSound: AlertTone = parent?.getItemAtPosition(position) as AlertTone
        preferences.currentAlertToneSaveKey = selectedSound.saveKey
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    companion object {
        lateinit var vibrateFocus: ShowcaseManager
        lateinit var soundFocus: ShowcaseManager

        fun showSoundFocus() = soundFocus.show()
        fun showVibrateFocus() = vibrateFocus.show()
    }

    fun buildFocus(view: View, keyText: String): ShowcaseManager {
        val builder = ShowcaseManager.Builder()
        return builder.context(context!!)
            .key(keyText)
            .developerMode(false)
            .view(view)
            //.descriptionImageRes(R.mipmap.ic_launcher)
            .descriptionTitle("Notification Method")
            .descriptionText("Toggle to enable")
            .buttonText("Next")
            .marginFocusArea(40)
            .add()
            .build()
        //.show()
    }
}

fun String.convertToFeedbackOption(): SettingsFragment.FeedbackOption {
    for (option in values()) {
        if (option.saveKey == this) {
            return option
        }
    }
    // If no option matched, set as New User
    return NEWUSER
}

fun String.convertToAlertTone(): SettingsFragment.AlertTone {
    for (tone in SettingsFragment.AlertTone.values()) {
        if (tone.saveKey == this) {
            return tone
        }
    }
    return Beep
}
