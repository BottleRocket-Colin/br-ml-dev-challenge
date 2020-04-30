package com.br.ml.brpathfinder.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.br.ml.brpathfinder.MainActivity
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.onboarding.OnboardingActivity
import com.br.ml.brpathfinder.settings.SettingsFragment
import com.br.ml.brpathfinder.settings.SettingsFragment.FeedbackOption.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

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
        return when (SettingsFragment.pullFeedbackOptionFromSharedPreferences(this)) {
            VIBRATE, SOUND, BOTH, NONE -> {
                // Go to the Main Activity
                Toast.makeText(this, "mainActivity", Toast.LENGTH_SHORT).show()
                Intent(this, MainActivity::class.java)
            }
            else -> {
                // Go to the onboarding flow
                // TODO - update this to correct Flow
                Toast.makeText(this, "onboarding", Toast.LENGTH_SHORT).show()
                Intent(this, OnboardingActivity::class.java)
            }
    }
    }
}