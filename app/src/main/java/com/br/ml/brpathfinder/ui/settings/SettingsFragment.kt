package com.br.ml.brpathfinder.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.br.ml.brpathfinder.R

class SettingsFragment : Fragment(), SettingsPresenter.Listener {

    private lateinit var settingsAvm: SettingsAvm
    private lateinit var presenterContainer: SettingsPresenter.Container

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.settings_fragment, container, false)

        settingsAvm = ViewModelProviders.of(this).get(SettingsAvm::class.java)

        presenterContainer = SettingsPresenter.Container(root, this)

        settingsAvm.setInSharedPrefs().observe(this, Observer {
            val sharedPref = context?.getSharedPreferences(
                getString(R.string.shared_prefs_tag),
                Context.MODE_PRIVATE
            )
            sharedPref?.edit()?.putInt("FEEDBACK_TYPE", it.ordinal)?.apply()
            Log.d("feedback_type", "${it.ordinal} saved to shared prefs")
        })
        settingsAvm.presenter()
            .observe(this, Observer {
                if (it != null) {
                    SettingsPresenter.present(presenterContainer, it)
                }
            })

        settingsAvm.setUp(getSharedPrefs())

        return root
    }

    private fun getSharedPrefs(): SharedPreferences? {
        return context?.getSharedPreferences(
            getString(R.string.shared_prefs_tag),
            Context.MODE_PRIVATE
        )
    }

    override fun onVibrateRadioChecked(isChecked: Boolean) {
        settingsAvm.onVibrateRadioChecked(isChecked)
    }

    override fun onSoundRadioChecked(isChecked: Boolean) =
        settingsAvm.onSoundRadioChecked(isChecked)

    override fun onBothVibrateAndSoundRadioChecked(isChecked: Boolean) =
        settingsAvm.onBothVibrateAndSoundRadioChecked(isChecked)

    override fun onNoFeedbackRadioChecked(isChecked: Boolean) {
        settingsAvm.onNoFeedbackRadioChecked(isChecked)
    }

    enum class FeedbackType {
        VIBRATE,
        SOUND,
        BOTHVIBRATEANDSOUND,
        NOFEEDBACK
    }
}
