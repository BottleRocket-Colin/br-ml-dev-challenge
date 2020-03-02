package com.br.ml.brpathfinder.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.br.ml.brpathfinder.MainActivity
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.common.FragmentName
import com.br.ml.brpathfinder.settings.SettingsFragment
import kotlinx.android.synthetic.main.activity_landing.*

class LandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        arfragLoadButton.setOnClickListener {
            startActivity(MainActivity.newIntent(this, FragmentName.AR_FRAGMENT))
        }

        mlfragLoadButton.setOnClickListener {
            startActivity(MainActivity.newIntent(this, FragmentName.ML_FRAGMENT))
        }

        settingsLoadButton.setOnClickListener {
            startActivity(MainActivity.newIntent(this, FragmentName.SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()

        // This is just a test to make sure that the shared prefs from the Settings page are accessible
        // Feel free to remove if needed
        setting_test.text = SettingsFragment.pullFeedbackOptionFromSharedPreferences(this)?.saveKey
    }
}
