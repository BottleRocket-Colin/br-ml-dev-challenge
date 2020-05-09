package com.br.ml.brpathfinder.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.br.ml.brpathfinder.MainActivity
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.onboarding.OnboardingActivity
import com.br.ml.brpathfinder.settings.SettingsFragment.FeedbackOption.*
import com.br.ml.brpathfinder.settings.convertToFeedbackOption
import com.br.ml.brpathfinder.utils.preferences.PreferencesImplementation

class SplashActivity : AppCompatActivity() {

    private lateinit var preferences: PreferencesImplementation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        preferences = PreferencesImplementation(baseContext)

        delayThenGo(2000, whereToGo())
    }

    private fun delayThenGo(delayLength: Long, intent: Intent) {
        val handler = Handler()
        handler.postDelayed({
            startActivity(intent)
            finish()
        }, delayLength)
    }

    private fun whereToGo(): Intent {
        // Check if the user needs to be assigned as new
        if (preferences.currentFeedbackMode.isBlank()) {
            preferences.currentFeedbackMode == NEWUSER.saveKey
        }
        return when (preferences.currentFeedbackMode.convertToFeedbackOption()) {
            VIBRATE, SOUND, BOTH, NONE -> {
                // Go to the Main Activity
                Intent(this, MainActivity::class.java)
            }
            else -> {
                // Go to the onboarding flow
                Intent(this, OnboardingActivity::class.java)
            }
        }
    }
}