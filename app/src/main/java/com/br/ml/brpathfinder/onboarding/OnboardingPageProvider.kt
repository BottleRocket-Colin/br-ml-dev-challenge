package com.br.ml.brpathfinder.onboarding

import androidx.fragment.app.Fragment
import com.br.ml.brpathfinder.settings.SettingsFragment
import com.br.ml.brpathfinder.ui.main.MainFragment
import com.cleveroad.slidingtutorial.TutorialPageProvider

class OnboardingPageProvider : TutorialPageProvider<Fragment> {

    override fun providePage(position: Int): Fragment {
        return when (position) {
            0 -> {
                OnboardingStartFragment()
            }
            1 -> {
                OnboardingSoundNotifSettingFragment()
            }
            2 -> {
                SettingsFragment()
            }
            3 -> {
                OnboardingVibrationNotifSettingFragment()
            }
            4 -> {
                SettingsFragment()
            }
            else -> {
                throw IllegalArgumentException("Unknown position: $position")
            }
        }
    }
}

