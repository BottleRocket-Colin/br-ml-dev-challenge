package com.br.ml.brpathfinder.onboarding

import androidx.fragment.app.Fragment
import com.cleveroad.slidingtutorial.TutorialPageProvider

class OnboardingPageProvider : TutorialPageProvider<Fragment> {

    override fun providePage(position: Int): Fragment {
        return when (position) {
            0 -> {
                OnboardingStartFragment()
            }
            1 -> {
                OnboardingSettingsVibrationInstructionsFragment()
            }
            2 -> {
                OnboardingSettingsVibrateFragment()
            }
            3 -> {
                OnboardingSettingsSoundInstructionsFragment()
            }
            4 -> {
                OnboardingSettingsSoundFragment()
            }
            else -> {
                throw IllegalArgumentException("Unknown position: $position")
            }
        }
    }
}

