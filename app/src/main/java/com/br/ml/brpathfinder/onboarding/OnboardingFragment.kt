package com.br.ml.brpathfinder.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.br.ml.brpathfinder.MainActivity
import com.br.ml.brpathfinder.R
//import com.br.ml.brpathfinder.settings.SettingsFragment
import com.br.ml.brpathfinder.utils.preferences.PreferencesImplementation
import com.cleveroad.slidingtutorial.*

class OnboardingFragment : TutorialSupportFragment(), OnTutorialPageChangeListener {
    private val TAG = "CustomTutorialSFragment"
    private val TOTAL_PAGES = 5

    private lateinit var preferences: PreferencesImplementation

    private val mOnSkipClickListener = View.OnClickListener { Toast.makeText(context, "Skip button clicked", Toast.LENGTH_SHORT).show() }

    private val mTutorialPageProvider: TutorialPageProvider<Fragment> = OnboardingPageProvider()

    private var pagesColors: IntArray? = null
    private var noRollback = false

    override fun onStop() {
        super.onStop()
        startActivity(Intent(context, MainActivity::class.java))
        preferences.completedOnboarding = true
        activity?.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferencesImplementation(requireContext())

        if (pagesColors == null) {
            val requireContext: Context? = context
            pagesColors = requireContext?.let {
                intArrayOf(
                        ContextCompat.getColor(requireContext, R.color.colorPurpleBackground),
                        ContextCompat.getColor(requireContext, R.color.colorPurpleBackground),
                        ContextCompat.getColor(requireContext, R.color.design_default_color_background),
                        ContextCompat.getColor(requireContext, R.color.colorPurpleBackground),
                        ContextCompat.getColor(requireContext, R.color.design_default_color_background)
                )
            }
        }
        addOnTutorialPageChangeListener(this)
    }

    override fun provideTutorialOptions(): TutorialOptions? {
        return newTutorialOptionsBuilder(requireContext())
                .setUseAutoRemoveTutorialFragment(true)
                .setUseInfiniteScroll(false)
                .setNoRollBack(noRollback)
                .setPagesColors(pagesColors!!)
                .setPagesCount(TOTAL_PAGES)
                .setIndicatorOptions(IndicatorOptions.newBuilder(requireContext())
                        .setElementSizeRes(R.dimen.indicator_size)
                        .setElementSpacingRes(R.dimen.indicator_spacing)
                        .setElementColorRes(android.R.color.darker_gray)
                        .setSelectedElementColor(Color.LTGRAY)
                        .build())
                .setOnSkipClickListener(mOnSkipClickListener)
                .setTutorialPageProvider(mTutorialPageProvider)
                .build()
    }

    override fun getLayoutResId(): Int {
        return R.layout.onboarding_fragment_layout
    }

    override fun onPageChanged(position: Int) {
        Log.i(TAG, "onPageChanged: position = $position")
        when (position) {
            EMPTY_FRAGMENT_POSITION -> {
                Log.i(TAG, "onPageChanged: Empty fragment is visible")
            }
            2 -> {
                OnboardingSettingsVibrateFragment.showVibrateFocus()
                Log.i(TAG, "onPageChanged: show vibrate focus")
            }
            4 -> {
                OnboardingSettingsSoundFragment.showSoundFocus()
                Log.i(TAG, "onPageChanged: show sound focus")
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(): OnboardingFragment {
            val args = Bundle()
            val fragment = OnboardingFragment()
            fragment.arguments = args
            return fragment
        }
    }
}