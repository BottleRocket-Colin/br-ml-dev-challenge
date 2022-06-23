package com.br.ml.brpathfinder.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.br.ml.brpathfinder.R
//import com.br.ml.brpathfinder.settings.SettingsFragment
import com.br.ml.brpathfinder.utils.preferences.PreferencesImplementation
import com.google.android.material.switchmaterial.SwitchMaterial
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType

class OnboardingSettingsSoundFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_onboarding_settings_sound, container, false)

        val soundSwitch: SwitchMaterial = view.findViewById(R.id
                .onboarding_settings_feedback_sound_switch)
        soundFocus = buildGuideView(soundSwitch, "Try it!")

//        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
//            // TODO: need some more testing
//            if (isChecked) {
//                if (preferences.currentFeedbackMode == SettingsFragment.FeedbackOption.VIBRATE
//                                .saveKey) {
//                    preferences.currentFeedbackMode = SettingsFragment.FeedbackOption.BOTH.saveKey
//                } else {
//                    preferences.currentFeedbackMode = SettingsFragment.FeedbackOption.SOUND.saveKey
//                }
//            } else {
//                if (preferences.currentFeedbackMode == SettingsFragment.FeedbackOption.BOTH
//                                .saveKey) {
//                    preferences.currentFeedbackMode = SettingsFragment.FeedbackOption.VIBRATE.saveKey
//                } else {
//                    preferences.currentFeedbackMode = SettingsFragment.FeedbackOption.NONE.saveKey
//                }
//            }
//        }

        return view
    }

    fun buildGuideView(view: View, keyText: String): GuideView {
        return GuideView.Builder(requireContext())
                .setTitle(keyText)
                .setContentText("Toggle here to enable SOUND notification")
                .setTargetView(view)
                .setDismissType(DismissType.anywhere)
//                .setContentTypeFace(Typeface)//optional
//                .setTitleTypeFace(Typeface)//optional
//                .setDismissType(DismissType.outSide) //optional - default dismissible by TargetView
                .build()
    }

    companion object {
        lateinit var soundFocus: GuideView
        fun showSoundFocus() = soundFocus.show()
    }
}